"""
ConsoleController: User interface for manually solving
Anne Hoy's problems from the console.
"""


# Copyright 2014, 2017 Dustin Wehr, Danny Heap, Bogdan Simion,
# Jacqueline Smith, Dan Zingaro
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


from toah_model import TOAHModel, IllegalMoveError


def move(model, origin, dest):
    """ Apply move from origin to destination in model.

    May raise an IllegalMoveError.

    @param TOAHModel model:
        model to modify
    @param int origin:
        stool number (index from 0) of cheese to move
    @param int dest:
        stool number you want to move cheese to
    @rtype: None
    """
    model.move(origin, dest)


class ConsoleController:
    """ Controller for text console.
    """

    def __init__(self, number_of_cheeses, number_of_stools):
        """ Initialize a new ConsoleController self.

        @param ConsoleController self:
        @param int number_of_cheeses:
        @param int number_of_stools:
        @rtype: None
        """
        self.model = TOAHModel(number_of_stools)
        self.model.fill_first_stool(number_of_cheeses)
        self.number_of_stools = number_of_stools

    def play_loop(self):
        """ Play Console-based game.

        @param ConsoleController self:
        @rtype: None

        """
        print("Instructions: To move a cheese from the first stool to "
              "the second, input '0 to 1' \n Enter 'ex' to exit")
        move_count = 1
        print(self.model)
        move_in = input('Enter Move ' + str(move_count) + ':')
        while move_in != 'ex':
            move_in = move_in.split()
            if len(move_in) == 3 and move_in[1] == 'to' and \
                    (move_in[0].isnumeric() and move_in[2].isnumeric()):
                try:
                    self.model.move(int(move_in[0]), int(move_in[-1]))
                    move_count += 1
                except IllegalMoveError as e:
                    print(e)
            else:
                print("Invalid Input Syntax")
            print(self.model)
            move_in = input('Enter Move ' + str(move_count) + ':')


if __name__ == '__main__':
    C = ConsoleController(5, 4)
    C.play_loop()
    # import python_ta
    # python_ta.check_all(config="consolecontroller_pyta.txt")
