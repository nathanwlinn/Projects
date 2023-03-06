#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "snake_utils.h"
#include "state.h"

/* Helper function definitions */
static void set_board_at(game_state_t* state, unsigned int x, unsigned int y, char ch);
static bool is_tail(char c);
static bool is_snake(char c);
static char body_to_tail(char c);
static unsigned int get_next_x(unsigned int cur_x, char c);
static unsigned int get_next_y(unsigned int cur_y, char c);
static void find_head(game_state_t* state, unsigned int snum);
static char next_square(game_state_t* state, unsigned int snum);
static void update_tail(game_state_t* state, unsigned int snum);
static void update_head(game_state_t* state, unsigned int snum);


/* Task 1 */
game_state_t* create_default_state() {
  // TODO: Implement this function. 
  // default state
  // This is to easily understand which values are which

  snake_t *initial_snakes = (snake_t *)malloc(sizeof(snake_t));
  game_state_t *initial_state = (game_state_t *)malloc(sizeof(game_state_t));
  initial_snakes->tail_x = 2;
  initial_snakes->tail_y = 2;
  initial_snakes->head_x = 4;
  initial_snakes->head_y = 2;
  int cols = 20;
  initial_state->num_rows = 18;
  initial_state->num_snakes = 1;
  initial_state->board = (char **)malloc(sizeof(char *) * initial_state->num_rows);
  initial_snakes->live = true;
  initial_state->snakes = initial_snakes;
  for (int x = 0; x < initial_state->num_rows; x++) {
	 initial_state->board[x] = (char *)malloc(sizeof(char) * (cols + 1));
	 if (x == 0 || x == initial_state->num_rows - 1) {
		 strcpy(initial_state->board[x], "####################");
	 } else {
		 strcpy(initial_state->board[x], "#                  #");
	 }
  }
  set_board_at(initial_state, 9, 2, '*');
  set_board_at(initial_state, 2, 2, 'd');
  set_board_at(initial_state, 3, 2, '>');
  set_board_at(initial_state, 4, 2, 'D');


  return initial_state;
}


/* Task 2 */
void free_state(game_state_t* state) {
  // TODO: Implement this function.
  for (int x = 0; x < state->num_rows; x++) {
	  free(state->board[x]);
  }
  free(state->snakes);
  free(state->board);
  free(state);
  return;
}

/* Task 3 */
void print_board(game_state_t* state, FILE* fp) {
  // TODO: Implement this function.
  for (int x = 0; x < state->num_rows; x++) {
	  fprintf(fp, "%s\n", state->board[x]);
  }
  return;
}

/*
  Saves the current state into filename. Does not modify the state object.
  (already implemented for you).
*/
void save_board(game_state_t* state, char* filename) {
  FILE* f = fopen(filename, "w");
  print_board(state, f);
  fclose(f);
}


/* Task 4.1 */

/*
  Helper function to get a character from the board
  (already implemented for you).
*/
char get_board_at(game_state_t* state, unsigned int x, unsigned int y) {
  return state->board[y][x];
}

/*
  Helper function to set a character on the board
  (already implemented for you).
*/
static void set_board_at(game_state_t* state, unsigned int x, unsigned int y, char ch) {
  state->board[y][x] = ch;
}

/*
  Returns true if c is part of the snake's tail.
  The snake consists of these characters: "wasd"
  Returns false otherwise.
*/
static bool is_tail(char c) {
  // TODO: Implement this function.
  if (c == 'w' || c == 's' || c == 'a' || c == 'd') {
	  return true;
  } else {
	  return false;
  }
}

/*
  Returns true if c is part of the snake's head.
  The snake consists of these characters: "WASD"
  Returns false otherwise.
*/
static bool is_head(char c) {
  // TODO: Implement this function.
  if (c == 'W' || c == 'S' || c == 'A' || c == 'D' || c == 'x') {
	  return true;
  } else {
	  return false;
  }
}

/*
  Returns true if c is part of the snake.
  The snake consists of these characters: "wasd^<>v"
*/
static bool is_snake(char c) {
  // TODO: Implement this function.
  if (is_tail(c) == true || is_head(c) == true) {
	  return true;
  }
  if (c == '^' || c == '<' || c == '>' || c == 'v') {
	  return true;
  }
  return false;
}

/*
  Converts a character in the snake's body ("^<>v")
  to the matching character representing the snake's
  tail ("wasd").
*/
static char body_to_tail(char c) {
  // TODO: Implement this function.
  if (c == '^') {
	  return 'w';
  }
  if (c == '<') {
	  return 'a';
  }
  if (c == '>') {
	  return 'd'; 
  }
  if (c == 'v') {
	  return 's';
  }
  return '?';
}

/*
  Converts a character in the snake's head ("WASD")
  to the matching character representing the snake's
  body ("^<>v").
*/
static char head_to_body(char c) {
  // TODO: Implement this function.
  if (c == 'W') {
	  return '^';
  }
  if (c == 'A') {
	  return '<';
  }
  if (c == 'D') {
	  return '>';
  }
  if (c == 'S') {
	  return 'v';
  }
  return '?';
}

/*
  Returns cur_x + 1 if c is '>' or 'd' or 'D'.
  Returns cur_x - 1 if c is '<' or 'a' or 'A'.
  Returns cur_x otherwise.
*/
static unsigned int get_next_x(unsigned int cur_x, char c) {
  // TODO: Implement this function.
  if (c == 'd' || c == 'D' || c == '>') {
	  return cur_x + 1;
  } else if (c == 'a' || c == 'A' || c == '<') {
	  return cur_x - 1;
  } else {
  return cur_x;
  }
}

/*
  Returns cur_y + 1 if c is '^' or 'w' or 'W'.
  Returns cur_y - 1 if c is 'v' or 's' or 'S'.
  Returns cur_y otherwise.
*/
static unsigned int get_next_y(unsigned int cur_y, char c) {
  // TODO: Implement this function.
  if (c == 'v' || c == 's' || c == 'S') {
	  return cur_y + 1;
  } else if (c == '^' || c == 'W' || c == 'w') {
	  return cur_y - 1;
  } else {
  return cur_y;
  }
}

/*
  Task 4.2

  Helper function for update_state. Return the character in the cell the snake is moving into.

  This function should not modify anything.
*/
static char next_square(game_state_t* state, unsigned int snum) {
  // TODO: Implement this function.
  int dir_x = state->snakes[snum].head_x;
  int dir_y = state->snakes[snum].head_y;
  char headc = get_board_at(state, dir_x, dir_y);
  char next_s = get_board_at(state, get_next_x(dir_x, headc), get_next_y(dir_y, headc));
  return next_s;
}


/*
  Task 4.3

  Helper function for update_state. Update the head...

  ...on the board: add a character where the snake is moving

  ...in the snake struct: update the x and y coordinates of the head

  Note that this function ignores food, walls, and snake bodies when moving the head.
*/
static void update_head(game_state_t* state, unsigned int snum) {
  // TODO: Implement this function.
  int xhead = state->snakes[snum].head_x;
  int yhead = state->snakes[snum].head_y;
  char headc = get_board_at(state, xhead, yhead);
  state->snakes[snum].head_x = get_next_x(xhead, headc);
  state->snakes[snum].head_y = get_next_y(yhead, headc);
  set_board_at(state, xhead, yhead, head_to_body(headc));
  set_board_at(state, state->snakes[snum].head_x, state->snakes[snum].head_y, headc);
  return;
}


/*
  Task 4.4

  Helper function for update_state. Update the tail...

  ...on the board: blank out the current tail, and change the new
  tail from a body character (^v<>) into a tail character (wasd)

  ...in the snake struct: update the x and y coordinates of the tail
*/
static void update_tail(game_state_t* state, unsigned int snum) {
  // TODO: Implement this function.
  int xtail = state->snakes[snum].tail_x;
  int ytail = state->snakes[snum].tail_y;
  char tailc = get_board_at(state, xtail, ytail);
  int xxtail = get_next_x(xtail, tailc);
  int yytail = get_next_y(ytail, tailc);
  char ctailc = get_board_at(state, xxtail, yytail);

  // Updating the states.
  state->snakes[snum].tail_x = xxtail;
  state->snakes[snum].tail_y = yytail;

  // Getting rid of previous tail part.
  set_board_at(state, xtail, ytail, ' ');
  set_board_at(state, xxtail, yytail, body_to_tail(ctailc));
  return;
}


/* Task 4.5 */
void update_state(game_state_t* state, int (*add_food)(game_state_t* state)) {
  // TODO: Implement this function.
  for (int x = 0; x < state->num_snakes; x++) {
	  char next_s = next_square(state, x);
	  if (next_s == '*') {
		  update_head(state, x);
		  add_food(state);
	  } else if (is_snake(next_s) == true || next_s == '#') {
		  state->snakes[x].live = false;
		  set_board_at(state, state->snakes[x].head_x, state->snakes[x].head_y, 'x');
	  } else {
		  update_head(state, x);
		  update_tail(state, x);
	  }
  }
  return;
}


/* Task 5 */
game_state_t* load_board(char* filename) {
  // TODO: Implement this function.
  game_state_t *game_state = (game_state_t *)malloc(sizeof(game_state_t));
  int new_rows = 0;
  int num_cols = 0;
  int placeholder = 0;
  char track;
  FILE  *fip = fopen(filename, "r");
  if (fip == NULL) {
	  exit(1);
  } else {
	  while (fscanf(fip, "%c", &track) != EOF) {
		  if (track == '\n') {
			  new_rows++;
		  }
		  if (new_rows == 0 && track != '\n') {
			  num_cols++;
		  }
	  }
  }
  fclose(fip);

  game_state->num_rows = new_rows;
  game_state->board = (char **)malloc(sizeof(char *) * new_rows);

  char tracker2;
  fip = fopen(filename, "r");
  if (fip != NULL) {
	  for (int x = 0; x < new_rows; x++) {
		  char *hold = (char *)malloc(sizeof(char) * (num_cols + 1));
		  fscanf(fip, "%[^\n]%*c", hold);
		  game_state->board[x] = (char *)malloc(sizeof(char)* (num_cols + 1));
		  strcpy(game_state->board[x], hold);
		  free(hold);
	  }
  }
  fclose(fip);
  return game_state;

}


/*
  Task 6.1

  Helper function for initialize_snakes.
  Given a snake struct with the tail coordinates filled in,
  trace through the board to find the head coordinates, and
  fill in the head coordinates in the struct.
*/
static void find_head(game_state_t* state, unsigned int snum) {
  // TODO: Implement this function.
  int posx = 0;
  int posy = 0;
  int tailx = state->snakes[snum].tail_x;
  int taily = state->snakes[snum].tail_y;
  char place = state->board[taily][tailx];
  char c;
  while (is_snake(place) == true) {
	  posx = tailx;
	  posy = taily;
	  c = state->board[posy][posx];
	  if (is_head(state->board[posy][posx]) == true) {
		  break;
	  }
	  if (is_tail(state->board[posy][posx]) == true || c == '^' || c == '<' || c == '>' || c == 'v') {
	  	tailx = get_next_x(tailx, place);
	  	taily = get_next_y(taily, place);
	  	place = state->board[taily][tailx];
	  }
  }
  state->snakes[snum].head_x = posx;
  state->snakes[snum].head_y = posy;
  return; 
}


/* Task 6.2 */
game_state_t* initialize_snakes(game_state_t* state) {
  // TODO: Implement this function.
  int ylength = state->num_rows;
  int num1 = 1;
  state->snakes = (snake_t *)malloc(sizeof(snake_t) * 1);
  int snakenum = 0;
  int num_snakes = 0;
  for (int x = 0; x < state->num_rows; x++) {
	  for (int y = 0; y < strlen(state->board[x]); y++) {
		  if (is_tail(state->board[x][y])) {
			  if (snakenum + 1 > num1) {
				  num1 = num1 * 2 + 1;
				  state->snakes = realloc(state->snakes, sizeof(snake_t) * num1);
			  }
			  state->snakes[snakenum].tail_x = y;
			  state->snakes[snakenum].tail_y = x;
			  find_head(state, snakenum);
			  snakenum++;
		  }
	  }
  }
  state->snakes = realloc(state->snakes, sizeof(snake_t) * snakenum);
  state->num_snakes = snakenum;
  return state;
}
