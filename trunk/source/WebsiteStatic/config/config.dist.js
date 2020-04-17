const _CONFIG = {
  meta: {
    debug_mode: false,
  },
  backend: {
    web_bridge_url: 'http://127.0.0.1:8080/api'
  },
  editor: {
    init_code: '// Enter code here ...',
    default_msg_orientation: "left"  // ["bottom" | "left"] the ultimate response messages default layout.
  },
  code_file_extensions: {
    c: '.c',
    boogie: '.bpl',
    c_pp: '.c',
    automata_script: '.ats',
    smt: '.smt2'
  },
  tools: [
    {
      name: "ULTIMATE Automizer",
      id: "automizer",
      description: "Verification of safety properties based on an automata-theoretic approach to software verification.",
      languages: ["Boogie", "C"],
      workers: [
        {
          language: "c",
          id: "cAutomizer",
          task_id: "AUTOMIZER_C",
          frontend_settings: [
            {
              name: "Check for memory leak in main procedure",
              id: "chck_main_mem_leak",
              plugin_id: "de.uni_freiburg.informatik.ultimate.plugins.generator.cacsl2boogietranslator",
              key: "Check for the main procedure if all allocated memory was freed",
              type: "bool",
              default: true
            },
            {
              name: "Check for overflows of signed integers",
              id: "chck_signed_int_overflow",
              plugin_id: "de.uni_freiburg.informatik.ultimate.plugins.generator.cacsl2boogietranslator",
              key: "Check absence of signed integer overflows",
              type: "bool",
              default: true
            }
          ]
        },
        {
          language: "boogie",
          id: "boogieAutomizer",
          task_id: "AUTOMIZER_BOOGIE",
          frontend_settings: [
            {
              name: "Check foo",
              id: "chck_foo",
              plugin_id: "de.uni_freiburg.informatik.ultimate.plugins.foo",
              key: "foo setting",
              type: "bool",
              default: true
            },
            {
              name: "Check bar",
              id: "chck_bar",
              plugin_id: "de.uni_freiburg.informatik.ultimate.plugins.bar",
              key: "bar setting",
              type: "bool",
              default: false
            }
          ]
        }
      ],
      logo_url: "img/tool_logo.png",
    },
    {
      name: "ULTIMATE Büchi Automizer",
      id: "buechi_automizer",
      description: "Termination analysis based on Büchi automata.",
      languages: ["Boogie", "C"],
      workers: [
        {
          language: "c",
          id: "cBuchiAutomizer",
          task_id: "TERMINATION_C",
          frontend_settings: []
        },
        {
          language: "boogie",
          id: "boogieBuchiAutomizer",
          task_id: "TERMINATION_BOOGIE",
          frontend_settings: []
        }
      ]
    },
    {
      name: "ULTIMATE Kojak",
      id: "kojak",
      description: "A software model checker.",
      languages: ["Boogie", "C"],
      workers: [
        {
          language: "c",
          id: "cKojak",
          task_id: "KOJAK_C",
          frontend_settings: []
        },
        {
          language: "boogie",
          id: "boogieKojak",
          task_id: "KOJAK_BOOGIE",
          frontend_settings: []
        }
      ]
    },
    {
      name: "ULTIMATE Taipan",
      id: "taipan",
      description: "Verification of safety properties using trace abstraction and abstract interpretation on path programs.",
      languages: ["Boogie", "C"],
      workers: [
        {
          language: "c",
          id: "cTaipan",
          task_id: "TAIPAN_C",
          frontend_settings: []
        },
        {
          language: "boogie",
          id: "boogieTaipan",
          task_id: "TAIPAN_BOOGIE",
          frontend_settings: []
        }
      ]
    },
    {
      name: "ULTIMATE LTL Automizer",
      id: "ltl_automizer",
      description: "An LTL software model checker based on Büchi programs.",
      languages: ["C_pp"],
      workers: [
        {
          language: "c_pp",
          id: "cLTLAutomizer",
          task_id: "LTLAUTOMIZER_C",
          frontend_settings: []
        }
      ]
    },
    {
      name: "ULTIMATE Lasso Ranker",
      id: "lasso_ranker",
      description: "Synthesis of ranking functions and nontermination arguments.",
      languages: ["Boogie", "C"],
      workers: [
        {
          language: "c",
          id: "cLassoRanker",
          task_id: "RANK_SYNTHESIS_C",
          frontend_settings: []
        },
        {
          language: "boogie",
          id: "boogieLassoRanker",
          task_id: "RANK_SYNTHESIS_BOOGIE",
          frontend_settings: []
        }
      ]
    },
    {
      name: "ULTIMATE Automata Library",
      id: "automata_library",
      description: "Nested Word Automta, Büchi Nested Word Automta, Petri Net, Alternating Finite Automata, Tree Automata.",
      languages: ["Automata_script"],
      workers: [
        {
          language: "automata_script",
          id: "automataScript",
          task_id: "AUTOMATA_SCRIPT",
          frontend_settings: []
        }
      ]
    },
    {
      name: "ULTIMATE Petri Automizer",
      id: "perti_automizer",
      description: "Petri net-based analysis of concurrent programs.",
      languages: ["Boogie"],
      workers: [
        {
          language: "boogie",
          id: "boogieConcurrentTraceAbstr",
          task_id: "CONCURRENT_BOOGIE",
          frontend_settings: []
        }
      ]
    },
    {
      name: "ULTIMATE Referee",
      id: "referee",
      description: "Checking validity of given invariants.",
      languages: ["Boogie", "C"],
      workers: [
        {
          language: "c",
          id: "cReferee",
          task_id: "REFEREE_C",
          frontend_settings: []
        },
        {
          language: "boogie",
          id: "boogieReferee",
          task_id: "REFEREE_BOOGIE",
          frontend_settings: []
        }
      ]
    },
    {
      name: "ULTIMATE Eliminator",
      id: "eliminator",
      description: "Run SMT script.",
      languages: ["Smt"],
      workers: [
        {
          language: "smt",
          id: "smtEliminator",
          task_id: "ELIMINATOR_SMT",
          frontend_settings: []
        }
      ]
    }
  ]
};
