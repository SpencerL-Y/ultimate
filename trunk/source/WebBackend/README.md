This WebBackend project is for serving the ultimate tools as a web-service.
The WebBackend application runs embedded jetty to provide an API for executing ultimate jobs.

# Deploy
Goto `trunk/source/BA_MavenParentUltimate` run `mvn clean install -P materialize`.
After a successful build, goto `trunk/source/BA_WebBackend`. The artifacts to run and configure the application are in `./target/products` (beside a copy of this README.md).

Now you need to configure the application.

# Configuration
## Initial configuration
First make a copy of the configuration files:

* Copy `web.config.properties.dist` to `web.config.properties`
* Copy `settings_whitelist.json.dist` to `settings_whitelist.json`

> Note: Copy the config files to a place outside of the target directory, since they will be lost on a rebuild.

Now add the path to your `web.config.properties` to the `WebBackend.ini` file (located at `./target/products/WebBackend/<plattform>/<ws>/<arch>/WebBackend.ini`):

Edit:
```ini
 -DWebBackend.SETTINGS_FILE=C:\path\to\your\web.config.properties
```

Then edit `web.config.properties` itself.

Mandatory:
```properties
 # SETTINGS_WHITELIST (string) : Path to a local user settings whitelist.
 SETTINGS_WHITELIST=C:\\path\\to\\your\\settings_whitelist.json
```

## Changing configuration
There are 2 ways of changing configuration settings. By the `web.config.properties` file or via VM arguments.

A setting "`SETTING_FOO`" in `web.config.properties` can be overridden via VM argument `-DWebBackend.SETTING_FOO=bar`.

## Default configuration

	# DEBUG (bool) .............. : True increases the verbosity of the logs.
	# PORT (int) ................ : determines the port the jetty server will be listening.
	# BACKEND_ROUTE (string) .... : The URL prefix, the API will be served at.
	#                               E.g. /api results in http://localhost:PORT/api
	# SETTINGS_WHITELIST (string) : Path to a local user settings whitelist.
	DEBUG=True
	PORT=8080
	BACKEND_ROUTE=/api
	SETTINGS_WHITELIST=C:\\path\\to\\settings_whitelist.json
	
	# SERVE_WEBSITE (bool) .... : True will also serve the Frontend.
	#                             If set to True, FRONTEND_PATH and FRONTEND_ROUTE should be set.
	# FRONTEND_PATH (string) .. : Path to the `WebsiteStatic` folder containing the Frontend.
	# FRONTEND_ROUTE (string) . : The URL prefix, the FRONTEND will be served at.
	#                             E.g. /website results in http://localhost:PORT/website
	SERVE_WEBSITE=True
	FRONTEND_PATH=C:\\path\\to\\WebsiteStatic
	FRONTEND_ROUTE=/website

## Whitelist for user settings
User settings can be allowed per plugin and key.

Create or edit the existing a `settings_whitelist.json`:

```json
{
	"plugin.id": ["key_foo", "key_bar"],
	"de.uni_freiburg.informatik.ultimate.plugins.analysis.syntaxchecker": [
		"remove filename from checker output"
	],
}
```

Ensure the path to ``settings_whitelist.json` is set correctly for the `SETTINGS_WHITELIST` setting.

## Serving the front-end (aka WebsiteStatic)
After a build, a cleaned, ready to be served Version of the `WebsiteStatic` project can be found in `trunk/source/BA_WebBackend/target/products/WebsiteStatic`.

### Bundeled with the backend
* Set the config-parameter `SERVE_WEBSITE` to `True`.
* Set the config-parameter `FRONTEND_PATH` to the absolute path of the "WebsiteStatic" folder.
* Configure the Website. See `trunk/source/WebsiteStatic/README.md` for details.

### Stand alone
* Serve the content of `WebsiteStatic` as a static Html
* Configure the Website. See `trunk/source/WebsiteStatic/README.md` for details.
