%module SwimLapPostProcessor

%{
#include "refine_lap_count.h"
%}

%include "stdint.i"
%include "std_vector.i"

namespace std {
	%template(LapStatVector) vector<lap_stats_t>;
}

typedef struct
{
  uint32_t nStrokes;
  uint32_t duration_in_10th_seconds;
  uint32_t lap_end_in_10th_seconds;
  uint32_t svm;
  
}lap_stats_t;

extern uint32_t refine_lap_count(std::vector<lap_stats_t> &lapStats);
