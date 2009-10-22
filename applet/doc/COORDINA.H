// coordinate_changes_pr.h -- Michael Maguire -- June 93

// function prototypes for coordinate_changes.c  -- screen to real coordinate
// 	changes and vice-versa */


void acknowledge_new_window_size( void ); /* must be called each time window size changes */

void change_view_by_this_screen_amount( short screenXShift, short screenYShift);

double screen_to_real_x( short screenX );
double screen_to_real_y( short screenY );

short real_to_screen_x( double realX );
short real_to_screen_y( double realY );

short real_to_screen_distance( double real_distance );
double screen_to_real_distance( short screen_distance );

