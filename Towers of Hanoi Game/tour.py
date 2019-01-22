"""
functions to run TOAH tours.
"""


# Copyright 2013, 2014, 2017 Gary Baumgartner, Danny Heap, Dustin Wehr,
# Bogdan Simion, Jacqueline Smith, Dan Zingaro
# Distributed under the terms of the GNU General Public License.
#
# This file is part of Assignment 1, CSC148, Winter 2017.
#
# This is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This file is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this file.  If not, see <http://www.gnu.org/licenses/>.
# Copyright 2013, 2014 Gary Baumgartner, Danny Heap, Dustin Wehr


# you may want to use time.sleep(delay_between_moves) in your
# solution for 'if __name__ == "main":'
import time
from toah_model import TOAHModel


def tour_of_four_stools(model, delay_btw_moves=0.5, animate=False):
    """Move a tower of cheeses from the first stool in model to the fourth.

    @type model: TOAHModel
        TOAHModel with tower of cheese on first stool and three empty
        stools
    @type delay_btw_moves: float
        time delay between moves if console_animate is True
    @type animate: bool
        animate the tour or not
    """
    move_cheeses(model, model.get_number_of_cheeses(), 0, 1, 2, 3)
    if animate:
        new_model = TOAHModel(4)
        new_model.fill_first_stool(model.get_number_of_cheeses())
        for i in range(model.number_of_moves()):
            new_model.move(model.get_move_seq().get_move(i)[0],
                           model.get_move_seq().get_move(i)[1])
            print(new_model)
            time.sleep(delay_btw_moves)


def min_moves(cheeses):
    """ Find the minimum possible moves for cheeses using four stools

    @param int cheeses:
    @rtype: int
    """
    if cheeses == 1:
        return 1
    else:
        possible_moves = []
        for i in range(1, cheeses):
            possible_moves.append(2 * min_moves(cheeses - i) + 2 ** i - 1)
        return min(possible_moves)


def find_i(cheeses):
    """ Find the i that produced the minimun number of moves in min_moves

    :param int cheeses:
    :rtype: int
    """
    min_ = min_moves(cheeses)
    possible_moves = []
    if cheeses > 1:
        while min_ not in possible_moves:
            for i in range(1, cheeses):
                possible_moves.append(2 * min_moves(cheeses - i) + 2 ** i - 1)
        return possible_moves.index(min_) + 1


def move_cheeses(model, n, src, inter, aux, dest):
    """Move n cheeses from source to destination

    @param TOAHModel model:
    @param int n:
    @param int src:
    @param int inter:
    @param int dest:
    @param int aux:
    @rtype: None
    """
    if n > 1:
        i = find_i(n)
        move_cheeses(model, n - i, src, dest, inter, aux)
        move_cheeses3(model, i, src, inter, dest)
        move_cheeses(model, n - i, aux, src, inter, dest)
    else:
        model.move(src, dest)


def move_cheeses3(model, n, source, intermediate, destination):
    """Move n cheeses from source to destination

    @param TOAHModel model:
    @param int n:
    @param int source:
    @param int intermediate:
    @param int destination:
    @rtype: None
    """
    if n > 1:
        move_cheeses3(model, n - 1, source, destination, intermediate)
        move_cheeses3(model, 1, source, intermediate, destination)
        move_cheeses3(model, n - 1, intermediate, source, destination)
    else:
        model.move(source, destination)


if __name__ == '__main__':
    num_cheeses = 10
    delay_between_moves = .5
    console_animate = True

    # DO NOT MODIFY THE CODE BELOW.
    four_stools = TOAHModel(4)
    four_stools.fill_first_stool(number_of_cheeses=num_cheeses)

    tour_of_four_stools(four_stools,
                        animate=console_animate,
                        delay_btw_moves=delay_between_moves)

    print(four_stools.number_of_moves())
    # Leave files below to see what python_ta checks.
    # File tour_pyta.txt must be in same folder
    import python_ta
    python_ta.check_all(config="tour_pyta.txt")
