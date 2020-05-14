const _CONFIG = {
  meta: {
	// debug_mode: if set to true, `test/result.json` will be used as a response for fetching ultimate results.
    debug_mode: false,
  },
  backend: {
	// web_bridge_url: URL to the WebBackend API.
    web_bridge_url: 'http://127.0.0.1:8080/api'
  },
  editor: {
	// Default content of the editor.
    init_code: '// Enter code here ...',
    // default_msg_orientation: one of ["bottom" | "left"], 
    //                          determines the ultimate response messages default orientation.
    default_msg_orientation: "left"
  },
  // code_file_extensions: Determines the file extension to be used as input for the ultimate tool.
  //                       The key is the language of the tool in the frontend; 
  //                       The value is the file extension to be used by ultimate.
  code_file_extensions: {
    c: '.c',
    boogie: '.bpl',
    c_pp: '.c',
    automata_script: '.ats',
    smt: '.smt2'
  },
  // tools: List of tool specific configurations. Each tool entry is a object like:
  /**
        id: "automizer",
        description: "Verification of ...",
        languages: ["Boogie", "C"],
        logo_url: "img/tool_logo.png",
        workers: [  // Each worker for this tool defines a language specific instance of the tool.
            {
                language: "c",  // Language mus be available in `code_file_extensions` settings.
            	id: "cAutomizer",  // Unique id for this worker.
                frontend_settings: [  // Frontend settings will be vailable to set by the user
                  {
                    name: "Check for memory leak in main procedure",  // The name in the settings menu.
                id: "chck_main_mem_leak",  // Any unique id of that setting.
                    // The Activator.PLUGIN_ID of the ultimate plugin.
                    plugin_id: "de.uni_freiburg.informatik.ultimate.plugins.foo",
                key: "the key" // The key as used by the plugin PreferenceInitializer LABEL_
                        type: "bool",  // Type [string] of this setting must be \in {'bool', }
                    default: true,
                    string: "/instance/de.uni_freiburg.informatik...." // To be used by the ultimate controller
          }
   */
  //  * Id (`id`).
  //  * Front-page enry (`name`, `description`, `languages`).
  //  * Supported languages and specific settings (`workers`).  
  tools: [
    {
      // name: A Human readable name of this tool. Used as Heading in the frontend.
      name: "ULTIMATE Automizer",
      // id: A mandatory unique id for the tool.
      id: "automizer",
      // description: Frontend description.
      description: "Verification of safety properties based on an automata-theoretic approach to software verification.",
      // languages: Supported languages to be displayed in the frontend.
      languages: ["Boogie", "C"],
      // workers: List of workers. Each worker for this tool defines a language specific toolchain.
      workers: [
        {
          // language: A Language that must be available in `code_file_extensions` settings.
          language: "c",
          // id: A mandatory unique id for this worker.
          id: "cAutomizer",
          // frontend_settings: A list of settings that will be available to set by the user specificly for this worker.
          frontend_settings: [
            {
              // name: Settings name displayed in the settings menu.
              name: "Check for memory leak in main procedure",
              // id: A mandatory unique id for this setting.
              id: "chck_main_mem_leak",
              // plugin_id: Ultimate plugin affected by this setting.
              plugin_id: "de.uni_freiburg.informatik.ultimate.plugins.generator.cacsl2boogietranslator",
              // key: Setting key as used by the plugin.
              key: "Check for the main procedure if all allocated memory was freed",
              // type: Setting type can be one of ("bool", )
              type: "bool",
              // default: Default state for the setting.
              default: true,
              // visible: If true, this setting is exposed to the user.
              visible: true
            },
            {
              name: "Check for overflows of signed integers",
              id: "chck_signed_int_overflow",
              plugin_id: "de.uni_freiburg.informatik.ultimate.plugins.generator.cacsl2boogietranslator",
              key: "Check absence of signed integer overflows",
              type: "bool",
              default: true,
              visible: true
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
              default: true,
              visible: true
            },
            {
              name: "Check bar",
              id: "chck_bar",
              plugin_id: "de.uni_freiburg.informatik.ultimate.plugins.bar",
              key: "bar setting",
              type: "bool",
              default: false,
              visible: true
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
