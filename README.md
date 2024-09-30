
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


## Thinking

3 groups 

View, Animation, Db events

View should always represent the state. So we give the state only after the animation

Let's put cards in one vector or in multiple?

Multiple is much easier to handle 

Single is more easy to render.

But if we animate anyway, we could do the multiple vectors

-> Animation -> Callback db event -> View re renders based on new state.

Animation should bring the object in the position it will re-render

This way we can make the game declarative based on state, and still have animations which will be purely side effects.

Animation needs a starting point and a finishing point.

Could we make animations events too?

[:animation/deal-cards 
  {:callback-event [:events/deal-cards]}]

What about giving the animation as a side-event? 

[:events/deal-cards 
   {:side-effect [:animation/deal-cards]}]