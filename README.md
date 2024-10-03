
Node.js example for [shadow-cljs](https://github.com/thheller/shadow-cljs)
----

### Develop

Watch & compile with with hot reloading:

```
./develop.sh
```


### Connect to VSCode Calva

1. `CMD + SHIFT + P`
2. Type `Connecting to a running REPL server in the ...`
3. Choose `shadow-cljs`
4. Use automatic port, or copy from terminal output after running `clj -X:dev`
5. Choose `:node-repl`
6. `Ctrl + Enter` to run function inline.
7. Enjoy.


### New strategy 

1. Events start animations. 
2. Cards are rendered once 
3. Cards positions setup once when state loads.
4. Each event triggers an event then an animation.
5. Cards are not coupled with their position.


