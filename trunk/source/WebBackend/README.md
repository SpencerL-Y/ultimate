# Configuration.
See [Config.java](./src/de/uni_freiburg/informatik/ultimate/web/backend/Config.java) for details.

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