import collections
import glob
import json
import os
import shutil

import os.path as osp

""" This script
* copies all available examples for the web ui into config/code_examples.
* Adds the code_examples.json index file used by the frontend to determine the available examples per worker.

Add new examples for a worker.id in the `tool_examples_map`
Follow the rules already available.
"""

HERE = osp.abspath(osp.dirname(__file__))
PROJECT_ROOT = osp.join(HERE, '..', '..')
EXAMPLES_DIR = osp.join(PROJECT_ROOT, 'examples')

tool_examples_map = {
  'boogieAutomizer': [
    {
      'path': osp.join(EXAMPLES_DIR, 'programs', 'toy', 'showcase'),
      'pattern': '*.bpl'
    }
  ],
  'cAutomizer': [
    {
      'path': osp.join(EXAMPLES_DIR, 'programs', 'toy', 'showcase'),
      'pattern': '*.c'
    },
    {
      'path': osp.join(EXAMPLES_DIR, 'programs', 'quantifier', 'regression', 'c'),
      'pattern': 'FunctionPointers01.c'
    }
  ],
  'cBuchiAutomizer': [
    {
      'path': osp.join(EXAMPLES_DIR, 'programs', 'termination', 'showcase'),
      'pattern': '*.c'
    }
  ],
  'boogieBuchiAutomizer': [
    {
      'path': osp.join(EXAMPLES_DIR, 'programs', 'termination', 'showcase'),
      'pattern': '*.bpl'
    }
  ],
  'cKojak': [
    {
      'path': osp.join(EXAMPLES_DIR, 'programs', 'toy', 'showcase'),
      'pattern': '*.c'
    },
    {
      'path': osp.join(EXAMPLES_DIR, 'programs', 'quantifier', 'regression', 'c'),
      'pattern': 'FunctionPointers01.c'
    }
  ],
  'boogieKojak': [
    {
      'path': osp.join(EXAMPLES_DIR, 'programs', 'toy', 'showcase'),
      'pattern': '*.bpl'
    }
  ],
  'cTaipan': [
    {
      'path': osp.join(EXAMPLES_DIR, 'programs', 'toy', 'showcase'),
      'pattern': '*.c'
    },
    {
      'path': osp.join(EXAMPLES_DIR, 'programs', 'quantifier', 'regression', 'c'),
      'pattern': 'FunctionPointers01.c'
    }
  ],
  'boogieTaipan': [
    {
      'path': osp.join(EXAMPLES_DIR, 'programs', 'toy', 'showcase'),
      'pattern': '*.bpl'
    }
  ],
  'cLTLAutomizer': [
    {
      'path': osp.join(EXAMPLES_DIR, 'LTL', 'coolant'),
      'pattern': '*'
    },
  ],
  'cLassoRanker': [
    {
      'path': osp.join(EXAMPLES_DIR, 'lassos'),
      'pattern': '*.c'
    },
  ],
  'boogieLassoRanker': [
    {
      'path': osp.join(EXAMPLES_DIR, 'lassos'),
      'pattern': '*.bpl'
    },
    {
      'path': osp.join(EXAMPLES_DIR, 'lassos', 'website'),
      'pattern': '*.bpl'
    },
  ],
  'automataScript': [
    {
      'path': osp.join(EXAMPLES_DIR, 'Automata', 'website'),
      'pattern': '*.ats'
    },
  ],
  'boogieConcurrentTraceAbstr': [
    {
      'path': osp.join(EXAMPLES_DIR, 'concurrent', 'concurrentBoogie', 'showcase'),
      'pattern': '*.bpl'
    },
  ],
  'cReferee': [
    {
      'path': osp.join(EXAMPLES_DIR, 'programs', 'toy', 'InvariantChecking', 'showcase'),
      'pattern': '*.c'
    },
  ],
  'boogieReferee': [
    {
      'path': osp.join(EXAMPLES_DIR, 'programs', 'toy', 'InvariantChecking', 'showcase'),
      'pattern': '*.bpl'
    },
  ],
  'smtEliminator': [
    {
      'path': osp.join(EXAMPLES_DIR, 'smtlib', 'QuantifierElimination'),
      'pattern': '*.smt2'
    },
  ],
}

if __name__ == '__main__':
    dest = osp.join(HERE, 'config', 'code_examples')
    if not osp.exists(dest):
        os.makedirs(dest)

    code_examples = collections.defaultdict(list)
    for tool, examples in tool_examples_map.items():
        for example in examples:
            path = example['path']
            pattern = example['pattern']
            for file in glob.glob(rf'{path}/{pattern}'):
                shutil.copy(file, dest)
                basename = osp.basename(file)
                code_examples[tool].append({
                  'name': (basename[:30] + '...') if len(basename) > 20 else basename,
                  'source': basename
                })

    with open(osp.join(HERE, 'config', 'code_examples', 'code_examples.json'), mode='w') as out_file:
        json.dump(code_examples, out_file)
