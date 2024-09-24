
Node.js example for [shadow-cljs](https://github.com/thheller/shadow-cljs)
----

### Develop

Watch & compile with with hot reloading:

```
npm install
clj -X:dev
```


### Connect to VSCode Calva

1. `CMD + SHIFT + P`
2. Type `Connecting to a running REPL server in the ...`
3. Choose `shadow-cljs`
4. Use automatic port, or copy from terminal output after running `clj -X:dev`
5. Choose `:node-repl`
6. `Ctrl + Enter` to run function inline.
7. Enjoy.


### Build

```
clj -X:prod
```

Build docker image, and push it two docker hub. (Multi-platform build arm64/amd64)

```
docker buildx build --platform linux/amd64,linux/arm64 -t paul931224/wizard:latest --push . 
```


### Deployment

Watchtower:

```
docker run -d --name watchtower --restart always -v /var/run/docker.sock:/var/run/docker.sock -v $HOME/.docker/config.json:/config.json:ro containrrr/watchtower --interval 30 wizard
docker pull containrrr/watchtower
```

Docker

```
docker pull paul931224/wizard
docker run -d -p 3000:3000 --name wizard index.docker.io/paul931224/wizard
```

Putting `docker.io` in the 