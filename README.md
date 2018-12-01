# proxy-monitor



## How

The following configuration is an example.

1.modify config.json

```json
{
  "sourcePort": 8066,
  "targetPort": 3306,
  "targetHost": "localhost"
}
```

proxy 's port 8066

mysql 's host "localhost"

mysql 's port 3306

2.run

```
cn.lightfish.proxy.ProxyMonitor
```

or

run.bat(when the project be packaged)

there will be a terminal.

3.open mysql client and connect with 8066

4.then test anything in ProxyMonitor's terminal!





## package

mvn package



## License

GNU GENERAL PUBLIC LICENSE

