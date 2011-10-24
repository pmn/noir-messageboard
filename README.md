# noir-messageboard

A sample messageboard  written in noir. 

This code is currently running at: http://noir-messageboard.heroku.com

This page contains good information for getting a Noir app running on Heroku: 
http://devcenter.heroku.com/articles/clojure-web-application

## Usage

```bash
lein deps
lein run
```

## Database connection string
This expects an environment variable called DATABASE URL. You can change this in utils/db.clj

The connection string looks like this: 

```
postgresql://username:password@localhost:5432/dbname
```

## Running migrations

```
lein run -m noir-messageboard.utils.migrations
```

## CSS

This sample uses Twitter Bootstrap, found here: 
http://twitter.github.com/bootstrap/


## License

MIT
http://www.opensource.org/licenses/mit-license.php
