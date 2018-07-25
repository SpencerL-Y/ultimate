#!/usr/bin/env python2.7
# -*- coding: UTF-8 -*-

import argparse
import collections
import datetime
import signal
import subprocess
import sys
import traceback
import urllib

import os


class LogLine:
    """
    level
    datetime
    location
    message
    timedelta
    """

    def __init__(self, line):
        self.line = line
        self.timedelta = datetime.timedelta()

        # Example: [2018-07-24 22:34:17,486 INFO  L167   ceAbstractionStarter]: Result for
        splitted = line.split()
        try:
            # example: 2018-07-24 19:22:46,708
            self.date = datetime.datetime.strptime((splitted[0] + ' ' + splitted[1])[1:], '%Y-%m-%d %H:%M:%S,%f')
            self.level = splitted[2]
            self.line_number = (splitted[3])[1:]
            self.location = splitted[4][:-2]
            self.message = ' '.join(splitted[5:])
        except:
            self.date = None
            self.level = None
            self.line_number = None
            self.location = None
            self.message = line

    def __str__(self):
        if len(self.line) > 120:
            return self.line[0:120] + '...'
        return self.line

    def construct_str(self):
        if self.date is not None:
            return ' '.join(
                [str(x) for x in
                 [self.timedelta.total_seconds(), self.date, self.level, self.line_number, self.location,
                  self.message]])
        else:
            return self.__str__()

    def construct_simple_str(self):
        return str(self.timedelta) + ' ' + self.line


class MyMatch:
    def __init__(self, log_line, lines_before=None, count_lines_before=0, count_lines_after=0):
        self.lines = []
        if count_lines_before > 0 or count_lines_after > 0:
            self.lines = ['--']
        if count_lines_before > 0:
            self.lines += lines_before
        self.count_lines_after = count_lines_after
        self.lines += [log_line]

    def add_line_after(self, line):
        if self.count_lines_after == 0:
            return False
        self.count_lines_after = self.count_lines_after - 1
        self.lines += [line]
        return True

    def print_match(self):
        for i in self.lines:
            if type(i) is str:
                print(i)
            else:
                print(i.construct_simple_str())


def signal_handler(sig, frame):
    print('Abort by user: you pressed Ctrl+C!')
    sys.exit(0)


def parse_args():
    argparser = argparse.ArgumentParser(description="Analyze a single Ultimate log file")
    argparser.add_argument('-f', '--file', metavar='file', required=True, help='The Ultimate log file')
    argparser.add_argument('-t', '--time-bound', type=int, dest='time_bound', metavar='seconds',
                           help='Show log lines with a time delta larger than this number in seconds (Default: 100)',
                           default=100)
    argparser.add_argument('-A', '--after', type=int, dest='lines_after', metavar='count',
                           help='Show additional log lines after a match (Default: 0)',
                           default=0)
    argparser.add_argument('-B', '--before', type=int, dest='lines_before', metavar='count',
                           help='Show additional log lines before a match (Default: 0)',
                           default=0)
    return argparser.parse_args()


def log_lines(file):
    with open(file) as f:
        lines = [line.rstrip('\n') for line in f]
        last_log_line = None
        for line in lines:
            current_log_line = LogLine(line)
            if last_log_line is not None:
                if current_log_line.date is not None and last_log_line.date is not None:
                    last_log_line.timedelta = current_log_line.date - last_log_line.date
                yield last_log_line
            last_log_line = current_log_line


def main():
    args = parse_args()
    log = log_lines(args.file)
    save_lines_before = args.lines_before if args.lines_before > 0 else 1
    past_buffer = collections.deque(maxlen=save_lines_before)
    matches = []
    for i in log:
        for m in matches:
            if not m.add_line_after(i):
                m.print_match()
                matches.remove(m)

        if i.timedelta.total_seconds() > args.time_bound:
            matches += [MyMatch(i, past_buffer, args.lines_before, args.lines_after)]

        past_buffer.append(i)


if __name__ == "__main__":
    signal.signal(signal.SIGINT, signal_handler)
    # just ignore pipe exceptions
    signal.signal(signal.SIGPIPE, signal.SIG_DFL)
    main()
