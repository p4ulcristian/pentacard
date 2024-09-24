const path = require("path");
const TerserPlugin = require('terser-webpack-plugin');

const config_frontend_ready = {
  name: "frontend_ready",
  entry: "./target/index.js",
  output: {
    path: path.resolve(__dirname, "public/js/libs"),
    filename: "node-modules.js",
    clean: true,
  },
  optimization: {
    minimize: true,
    minimizer: [new TerserPlugin({
      terserOptions: {
        compress: {
          dead_code: true,
        },
        output: {
          comments: false,
          beautify: false,
          ascii_only: true,
          quote_style: 1,
          semicolons: false,
        },
      },
    })],
  },
  module: {
    rules: [
      {
        test: /\.(js|jsx)$/i,
        loader: "babel-loader",
      },
      {
        test: /\.(eot|svg|ttf|woff|woff2|png|jpg|gif)$/i,
        type: "asset",
      },
    ],
  },
};

module.exports = config_frontend_ready;
