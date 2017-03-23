const path = require('path');
const webpack = require('webpack');

module.exports = function (env) {
  const bundleNameTemplate = '[hash].[name].bundle.js';
  const baseOutputPath = env.baseOutputPath;
  const compress = !env.noCompress;

  return [
    getDevelopmentConfiguration(bundleNameTemplate, baseOutputPath),
    getProductionConfiguration(bundleNameTemplate, baseOutputPath, compress)
  ];
};

function getProductionConfiguration(bundleNameTemplate, baseOutputPath, compress) {
  const productionConfig = getCommonConfiguration(bundleNameTemplate, baseOutputPath, '/[name]-manifest.json');

  productionConfig.output.publicPath = '/ui/';

  // baseOutputPath isn't set when running webpack inside the development server, but we don't need it there
  if (baseOutputPath) {
    productionConfig.output.path = baseOutputPath;
  }

  productionConfig.plugins.push(new webpack.IgnorePlugin(/^(redux-logger|react-addons-perf)$/));
  productionConfig.plugins.push(new webpack.DefinePlugin({
    'process.env': {
      NODE_ENV: JSON.stringify('production')
    }
  }));

  if (compress) {
    productionConfig.plugins.push(new webpack.optimize.UglifyJsPlugin({
      sourceMap: true,
      mangle: false,
      compress: {
        warnings: false,
      },
      output: {
        comments: false,
      }
    }));
  }

  return productionConfig;
}

function getDevelopmentConfiguration(bundleNameTemplate, baseOutputPath) {
  const developmentConfig = getCommonConfiguration(bundleNameTemplate, baseOutputPath, '/dev/[name]-manifest.json');

  developmentConfig.output.publicPath = '/ui/dev/';

  // baseOutputPath isn't set when running webpack inside the development server, but we don't need it there
  if (baseOutputPath) {
    developmentConfig.output.path = path.join(baseOutputPath, 'dev');
  }

  return developmentConfig;
}

function getCommonConfiguration(bundleNameTemplate, baseOutputPath, manifestFileTemplate) {
  return {
    cache: true,
    devtool: '#source-maps',
    entry: {
      // add new common libraries to this list to have them included in the common bundle
      // and not needed to be rebuilt every time app code changes
      commons: [
        'core-js',
        'history',
        'immutable',
        'jquery',
        'lodash',
        'moment',
        'moment-timezone',
        'numeral',
        'redux',
        'react',
        'react-clipboard.js',
        'react-dom',
        'react-draggable',
        'react-google-maps',
        'react-redux',
        'react-router',
        'react-router-redux',
        'react-select',
        'react-tooltip',
        'react-proxy',
        'react-virtualized',
        'redbox-react',
        'regenerator-runtime',
        'stacktrace-js',
        'stacktrace-gps',
        'velocity-animate',
        'velocity-react',
        'vis',
        'whatwg-fetch',
        'classnames/index.js'
      ]
    },
    output: {
      filename: bundleNameTemplate,
      path: baseOutputPath,
      // the name of the global variable which the library's require() function will be assigned to
      library: '[name]_lib',
      sourceMapFilename: `${bundleNameTemplate}.json`
    },
    plugins: [
      new webpack.DllPlugin({
        // the path to the manifest file which maps between modules included in a bundle and the
        // internal IDs within that bundle
        path: baseOutputPath + manifestFileTemplate,
        // the name of the global variable which the library's require function has been assigned to.
        // this must match the output.library option above
        name: '[name]_lib'
      })
    ]
  };
}
