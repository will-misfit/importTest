//
//  refine_lap_count.h
//  proc_swim_files
//
//  Created by james ochs on 6/28/15.
//  Copyright (c) 2015 james_ochs. All rights reserved.
//

#ifndef __proc_swim_files__refine_lap_count__
#define __proc_swim_files__refine_lap_count__

#include <stdio.h>
#include <vector>

typedef struct
{
  uint32_t nStrokes;
  uint32_t duration_in_10th_seconds;
  uint32_t lap_end_in_10th_seconds;
  uint32_t svm;
  
}lap_stats_t;





extern uint32_t refine_lap_count(std::vector<lap_stats_t> &lapStats);


#endif /* defined(__proc_swim_files__refine_lap_count__) */
