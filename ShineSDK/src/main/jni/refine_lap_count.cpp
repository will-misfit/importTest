//
//  refineLapCounts.cpp
//  swimlaptest
//
//  Created by james ochs on 12/6/14.
//  Copyright (c) 2014 Misfit. All rights reserved.
//

#include "refine_lap_count.h"
#include "stdlib.h"
#include <vector>
#include<algorithm>
#include<math.h>

using namespace std;

#define WINDOW_LENGTH 10

static const struct
{
  uint8_t winlen;
  double missed1thresh;
  double tooclosethresh;
  double extralapthresh;
  double ref2extra;
  double ref2missed;
  double finalRefine;
  double evenonly;
  
}params =
{
  .winlen = WINDOW_LENGTH,
  .missed1thresh = 1.8,
  .tooclosethresh = .75,
  .extralapthresh = 1.2,
  .ref2extra = .6,
  .ref2missed = 1.6,
  .finalRefine = 1,
  .evenonly = 1
};


using namespace std;

static double median(vector<uint32_t> &v, int32_t startIdx, int32_t endIdx);
static double median(int32_t *x, int n);



uint32_t refine_lap_count(vector<lap_stats_t> &lapStats)
{
  uint32_t win=params.winlen;
  uint32_t winEndIdx = win-1;
  uint32_t curLapIdx = 1;
  double groupMedian;
  double groupStrokeMedian;
  
  vector<uint32_t> lapLen;
  vector<uint32_t> lapIdx;
  vector<uint32_t> strokes;
  
  
  lapLen.clear(); lapIdx.clear(); strokes.clear();
  lapLen.reserve(lapStats.size());
  lapIdx.reserve(lapStats.size());
  strokes.reserve(lapStats.size());
  
  for(uint32_t i=0; i<lapStats.size(); i++)
  {
    lapLen.push_back(lapStats[i].duration_in_10th_seconds);
    lapIdx.push_back(lapStats[i].lap_end_in_10th_seconds);
    strokes.push_back(lapStats[i].nStrokes);
  }
  lapStats.clear();
  
  
  /*FILE *lapInFile = fopen("/users/jamesochs/tmp/lapsIn.csv","w");
   FILE *lapOutFile = fopen("/users/jamesochs/tmp/lapsOut.csv","w");
   
   for(int32_t k=0; k<lapLen.size(); k++)
   {
   fprintf(lapInFile,"%d,%d\n",lapLen[k],lapIdx[k]);
   }
   fclose(lapInFile);*/
  
  
  
  while(winEndIdx<lapLen.size())
  {
    uint32_t startIdx = winEndIdx-win+1;
    
    
    groupMedian = median(lapLen,startIdx,winEndIdx);
    groupStrokeMedian = median(strokes,startIdx,winEndIdx);
    
    
    double normLen = lapLen[curLapIdx]/groupMedian;
    double prevNormLen = lapLen[curLapIdx-1]/groupMedian;
    
    if(normLen > params.missed1thresh) //%look for potential missed strokes
    {
      
      //%if we had a short then long stroke, we may have not missed it,
      //%but instead counted the previous one too early, so adjust the
      //%length of the current stroke based on how far the previous stroke
      //%was from the median for this window
      normLen = normLen - (1-prevNormLen);
      
      //%the number missed is how many round integers we are away
      //%from the median
      int32_t nMissed = round(normLen-1);
      
      if(nMissed > 0)
      {
        //%insert missed strokes, and just assume they where median
        //%length long
        lapLen[curLapIdx] = round(groupMedian);
        strokes[curLapIdx] = round(groupStrokeMedian);
        uint32_t lapIdxTmp = lapIdx[curLapIdx];
        
        int32_t i;
        for(i=0; i<nMissed-1; i++)
        {
          lapLen.insert(lapLen.begin()+curLapIdx+i,(uint32_t)round(groupMedian));
          lapIdx.insert(lapIdx.begin()+curLapIdx+i,lapIdxTmp - (i+1)*round(groupMedian));
          strokes.insert(strokes.begin()+curLapIdx+i,(uint32_t)round(groupStrokeMedian));
          
        }
        int32_t lastInsertIdx = lapIdxTmp - nMissed*round(groupMedian);
        if(lastInsertIdx - lapIdx[curLapIdx-1] >= params.tooclosethresh*groupMedian)
        {
          lapLen.insert(lapLen.begin()+curLapIdx+nMissed-1,(uint32_t)round(groupMedian));
          lapIdx.insert(lapIdx.begin()+curLapIdx+nMissed-1,lastInsertIdx);
          strokes.insert(strokes.begin()+curLapIdx+nMissed-1,(uint32_t)round(groupStrokeMedian));
        }
      }
    }
    //%otherwise look for two consective laps that are short and merge them
    else if(normLen + prevNormLen < params.extralapthresh)
    {
      lapIdx.erase(lapIdx.begin()+curLapIdx);
      lapLen.erase(lapLen.begin()+curLapIdx);
      strokes.erase(strokes.begin()+curLapIdx);
      lapLen[curLapIdx-1] = round(groupMedian);
      strokes[curLapIdx-1] = round(groupStrokeMedian);
    }
    
    curLapIdx = curLapIdx+1;
    winEndIdx = winEndIdx+1;
  }
  

  for(size_t i=0; i<lapLen.size(); i++)
  {
    lap_stats_t l;
    l.svm = 0;
    l.duration_in_10th_seconds = lapLen[i];
    l.lap_end_in_10th_seconds = lapIdx[i];
    l.nStrokes = strokes[i];
    lapStats.push_back(l);
    
  }
  
  
  int32_t totalLaps = (int32_t)lapLen.size();
  double mLap = median(lapLen,0,(int32_t)lapLen.size()-1);
  if(params.finalRefine)
  {
    //%a final refinement remove a bit for short strokes
    //%and add a bit for long strokes
    int32_t extra = 0;
    int32_t missed = 0;
    for(int32_t k=0; k<lapLen.size(); k++)
    {
      extra += lapLen[k] < params.ref2extra*mLap;
      missed += lapLen[k] > params.ref2missed*mLap;
    }
    totalLaps = totalLaps-extra+missed;
  }
  
  //%params.evenonly
  if(params.evenonly && totalLaps%2 != 0)
  {
    double ave = 0.0;
    for(int32_t k=0; k<lapLen.size(); k++)
    {
      ave += lapLen[k];
    }
    ave /= lapLen.size();
    
    if(ave > mLap)
    {
      totalLaps--;
    }
    else
    {
      totalLaps++;
    }
  }
  
  
  return (uint32_t)totalLaps;
  
}



double median(vector<uint32_t> &v, int32_t startIdx, int32_t endIdx)
{
  
  int32_t len = endIdx-startIdx+1;
  int32_t *tmpV = new int32_t[len];
  int32_t j=0;
  for(int32_t i=startIdx; i<=endIdx; i++)
  {
    tmpV[j++] = v[i];
  }
  double ret = median(tmpV,len);
  delete[] tmpV;
  return ret;
}

//////////////////////////////////////////////////////////////////
//	median
//
//	Description:	calculate median value of an array, based on Quickselect method
//                runs in average linear time
//
//	Inputs:	x - input array
//			n - signal length
//	Outputs: Returns a[median] median value of array
//			x - partially sorted array
//
//	Note:	output will not be fully sorted as this is not necessary for median calculation
//////////////////////////////////////////////////////////////////
double median(int32_t *x, int n)
{
  int k = n/2;
  
  if(n==0)
  {
    return 0; //?
  }
  else if(n == 1)
  {
    return x[0];
  }
  else if(n == 2)
  {
    return (x[0] + x[1])/2.0;
  }
  
  
  int left = 0;
  int right = n-1;
  
  //we stop when our indicies have crossed
  while (left < right){
    
    int pivot = (left + right)/2; //this can be whatever
    int pivotValue = x[pivot];
    int storage=left;
    
    x[pivot] = x[right];
    x[right]=pivotValue;
    for(int i =left; i < right; i++){//for each number, if its less than the pivot, move it to the left, otherwise leave it on the right
      if(x[i] < pivotValue){
        int temp =x[storage];
        x[storage] = x[i];
        x[i]=temp;
        storage++;
      }
    }
    x[right]=x[storage];
    x[storage]=pivotValue;//move the pivot to its correct absolute location in the list
    
    //pick the correct half of the list you need to parse through to find your K, and ignore the other half
    if(storage < k)
      left = storage+1;
    else//storage>= k
      right = storage;
    
  }
  
  if(n % 2 == 0)
  {
    
    int maxVal = x[k-1];
    for(int i=0; i<k; i++)
    {
      if(x[i] > maxVal)
      {
        maxVal = x[i];
      }
    }
    return (double)(x[k] + maxVal)/2.0;
  }
  else
  {
    return x[k];
  }
  
}
