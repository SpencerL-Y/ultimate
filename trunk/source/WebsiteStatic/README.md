**Ultimate** framework website. Provides a frontend for the ultimate tools.

# Configuration and setup.
* Copy [config/config.dist.js](config/config.dist.js) to `config/config.js`.

Configuration is set in  [config/config.js](config/config.js). 

## Configuration sections:
**0. Meta:**
```javascript
...
meta: {
    debug_mode: false,  // if set to true, `test/result.json` will be used as a response for fetching ultimate results.
}
...
```


**1. Backend:**
```javascript
...
    backend: {
        web_bridge_url: 'http://127.0.0.1:8080/api' // URL to the WebInterface jetty backend server.
    },
...
```

**2. Editor:**
```javascript
...
    editor: {
        init_code: '// Enter code here ...'  // The default content of the editor.
        default_msg_orientation: "left"      // ["bottom" | "left"] the ultimate response messages default orientation.
    },
...
```

**3. Language file extension mappings:**
Determines the file extension to be used as input for the ultimate tool.
The key is the language of the tool in the frontend; The value is the file extension to be used.
```javascript
...
code_file_extensions: {
    c: '.c',  // Workers for language `c` will use `.c` as file extension.
    ...
}
...
```

**3. Tools:**
Tool specific configurations. For each tool we must provide configuration for its:
* Id (`id`).
* Front-page enry (`name`, `description`, `languages`).
* Supported languages and specific settings (`workers`).  
```javascript
    ...
    tools: [
        {
            id: "automizer",  // Unique id of the tool.
            name: "ULTIMATE Automizer",  // Human readable name of this tool.
            description: "Verification of ...",  // Frontend description.
            languages: ["Boogie", "C"],  // Supported languages to be displayed in the frontend.
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
                      },
                      ...
                    ]
                },
                {
                    name: "boogie",
                    id: "boogieAutomizer"
                }
            ]
        },
        ...
```

## Code Examples
Code examples are generated using the [build_examples.py](build_examples.py) script.
To add new examples edit the `tool_examples_map` dict accordingly.

## Tool details page
Each tool is associated with a details page. To alter its content, edit the page matching the `tool_id` in the
[config/too_pages](config/tool_pages) folder.

## Home page contents
The content sections are determined by the files in [config/home_page](config/home_page).

# Development
## Webbridge API
To fetch results from the ultimate tool for the interactive part, a POST request is sent to the URL defined in the
config section `backend.web_bridge_url`.

**POST request data:** (see `tool_interface.get_execute_settings()`)
```json
{
    "action": "execute",
    "code": "the code in the editor",
    "toolchain": {
      "id": _CONFIG.context.current_worker.id,
      "task_id": _CONFIG.context.current_worker.task_id,
    },
    "user_settings": {
      // The settings derrived from config `worker.frontend_settings` and set by the user.
    }
  }
```

**Expected result response example:**
```json
"results": [
    {
      "endCol": -1,
      "endLNr": -1,
      "logLvl": "warning",
      "longDesc": "unknown boogie variable #StackHeapBarrier",
      "shortDesc": "Unfinished Backtranslation",
      "startCol": -1,
      "startLNr": -1,
      "type": "warning"
    },
    ...
]
```

## _CONFIG.context
Contains

* `_CONFIG.context.tool` The config of the tool selected by the user.
* `_CONFIG.context.worker` The config of the tools worker selected by the user.
* `_CONFIG.context.url` The url parameters.


## Frontend ULR parameters
The URL can contain up to two parameters:

```
/?ui={int|tool|home}?tool={automizer| ...} 

ui=home : show the landing page. (By default).
ui=tool : show informations about the tool.
ui=int  : show the interactive tool interface. (The editor).
```

## Configure theme & style
### Bootstrap theming
Edit [bootstrap_dev/scss/main.scss](bootstrap_dev/scss/main.scss) and then run `npm run css` to apply changes.


# Dependencies
* [ace-editor](https://ace.c9.io/)
* [jquery](https://jquery.com/)
* [handlebars](https://handlebarsjs.com/)
* [bootstrap](https://getbootstrap.com/)
