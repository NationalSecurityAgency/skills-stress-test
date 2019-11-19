const path = require('path');
const fs = require('fs');

const resolve = dir => path.join(__dirname, dir);

const getTarget = () => {
  let target = 'http://localhost:8880';
  const proxySslKeyLocation = process.env.WEBPACK_DEV_SERVER_PROXY_TABLE_SSL_KEY_PATH;
  const proxyCertKeyLocation = process.env.WEBPACK_DEV_SERVER_PROXY_TABLE_SSL_CERT_PATH;
  if (fs.existsSync(proxySslKeyLocation) && fs.existsSync(proxyCertKeyLocation)) {
    target = {
      host: 'localhost',
      port: 8443,
      key: fs.readFileSync(proxySslKeyLocation),
      cert: fs.readFileSync(proxyCertKeyLocation),
      protocol: 'https:'
    };
  }
  return target;
};

const proxyConf = {
  target: getTarget(),
  secure: false,
  changeOrigin: true,
  logLevel: 'debug',
};

module.exports = {
  devServer: {
    host: 'localhost',
    port: 8082,
    overlay: true,
    proxy: {
      '/stress': proxyConf,
    },
  },
  configureWebpack: {
    resolve: {
      alias: {
        '@$': resolve('src'),
      },
    },
    devtool: 'cheap-module-eval-source-map',
    // optimization: { minimize: false },
  },

  outputDir: undefined,
  assetsDir: 'static',
  runtimeCompiler: true,
  productionSourceMap: undefined,
  parallel: undefined,
  css: undefined,
};
