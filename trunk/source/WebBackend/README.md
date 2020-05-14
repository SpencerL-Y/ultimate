This WebBackend project is for serving the ultimate tools as a web-service.
The WebBackend application runs embedded jetty to provide an API for executing ultimate jobs.

# Overview


# Deploy
Goto `trunk/source/BA_WebBackend` run `mvn clean install -P materialize`. 
After a successful build, the artifacts to run and config the application are in `./target/products` (also  a copy of this README.md).

Now you need to configure the application.

# Configuration.
## Initial configuration.
First make a copy of the config files:

* Copy `web.config.properties.dist` to `web.config.properties.dist`
* Copy `settings_whitelist.json.dist` to `settings_whitelist.json`

> Note: Copy the config files to a place outside of the target directory, since they will be lost on rebuild.

Now add the path to your `web.config.properties.dist` to the `WebBackend.ini` file (located in `./target/products/WebBackend/<plattform>/<arch>/`):

Edit:
```ini
-DWebBackend.SETTINGS_FILE="C:\path\to\your\web.config.properties"

```


See [Config.java](./src/de/uni_freiburg/informatik/ultimate/web/backend/Config.java) for implementation details.

## Changing the Port
The default is 8080. To change it, alter the `PORT` config. e.g.

    -DWebBackend.PORT=8888

## Changing the URLs
To change the URL slug for the backend API alter the `BACKEND_ROUTE` config. The default is `/api`. e.g.

    -DWebBackend.BACKEND_ROUTE="/api"

You need to ensure, that the route matches the config.js setting of websiteStatic/config/config.js to work.

To change the websites URL, change the `FRONTEND_ROUTE` setting. The default is `/website`. e.g.

    -DWebBackend.FRONTEND_ROUTE="/ultimate-demo"

## Serving the front-end (aka Website).
Set the config-parameter `SERVE_WEBSITE` to `true`. e.g.

    -DWebBackend.SERVE_WEBSITE=true

Set the config-parameter `FRONTEND_PATH` to the absolute path of the "WebsiteStatic" project home folder. e.g.

    -DWebBackend.FRONTEND_PATH="/path/to/trunk/source/WebsiteStatic"

# Whitelist for user settings.
User settings can be allowed per plugin and key.

Create a `whitelist.json`:

```json
{
	"plugin.id": ["key_foo", "key_bar"],
	"de.uni_freiburg.informatik.ultimate.plugins.analysis.syntaxchecker": [
		"remove filename from checker output"
	],
}
```

Set `-DWebBackend.SETTINGS_WHITELIST="/path/to/your/whitelist.json"`