const path = require('path');
const webpack = require('webpack');
const CaseSensitivePathsPlugin = require('case-sensitive-paths-webpack-plugin');

const manifestFileName = 'commons-manifest.json';

module.exports = function (env) {
  const bundleNameTemplate = env.development ? '[name].bundle.js' : '[hash].[name].bundle.js';
  const baseOutputPath = env.baseOutputPath;
  const compress = !env.noCompress;
  const isDevelopmentServer = env.devServer;
  const configs = [getDevelopmentConfiguration(bundleNameTemplate, baseOutputPath, isDevelopmentServer)];

  if (!env.debugJs) {
    configs.push(getProductionConfiguration(bundleNameTemplate, baseOutputPath, compress, isDevelopmentServer));
  }

  return configs;
};

function getProductionConfiguration(bundleNameTemplate, baseOutputPath, compress, isDevelopmentServer) {
  const productionConfig = getCommonConfiguration(bundleNameTemplate, baseOutputPath, false, isDevelopmentServer);

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
      mangle: false, // need mangle=false otherwise Uglify will mangle the CSS classnames in JSX code
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

function getDevelopmentConfiguration(bundleNameTemplate, baseOutputPath, isDevelopmentServer) {
  const developmentConfig = getCommonConfiguration(bundleNameTemplate, baseOutputPath, true, isDevelopmentServer);

  developmentConfig.output.publicPath = '/ui/dev/';

  // baseOutputPath isn't set when running webpack inside the development server, but we don't need it there
  if (baseOutputPath) {
    developmentConfig.output.path = path.join(baseOutputPath, 'dev');
  }

  return developmentConfig;
}

function getCommonConfiguration(bundleNameTemplate, baseOutputPath, isDevelopment, isDevelopmentServer) {
  const mainDirectory = path.resolve(__dirname, 'src', 'main');
  const dllOutputDirectory = path.resolve(baseOutputPath, '..', 'webpack-dll');

  const config = {
    bail: true,
    cache: true,
    devtool: '#source-maps',
    entry: {
      scanActivationRequestPage: createPageEntries('ScanActivationPage', isDevelopmentServer),
      scanActivationKeyExpiredPage: createPageEntries('ScanActivationKeyExpiredPage', isDevelopmentServer),
      scanPage: createPageEntries('ScanPage', isDevelopmentServer),
      scanListPage: createPageEntries('ScanListPage', isDevelopmentServer),
      adminScanListPage: createPageEntries('AdminScanListPage', isDevelopmentServer),
      adminExampleListPage: createPageEntries('AdminExampleListPage', isDevelopmentServer),
      adminUserManagementPage: createPageEntries('AdminUserManagementPage', isDevelopmentServer),
      errorPage: createPageEntries('ErrorPage', isDevelopmentServer),
      helpPage: createPageEntries('HelpPage', isDevelopmentServer),
      usagePage: createPageEntries('UsagePage', isDevelopmentServer),
      comparisonPage: createPageEntries('ComparisonPage', isDevelopmentServer)
    },
    output: {
      filename: bundleNameTemplate,
      sourceMapFilename: `${bundleNameTemplate}.json`
    },
    module: {
      rules: [
        {
          test: /\.(js|jsx)/,
          include: [mainDirectory],
          use: createJsxLoaders(isDevelopmentServer)
        },
        {
          test: /\.json/,
          include: [mainDirectory],
          use: [{ loader: 'json-loader' }]
        },
        {
          test: /\.scss$/,
          include: [mainDirectory],
          use: [
            { loader: 'style-loader' },
            { loader: 'css-loader' },
            { loader: 'resolve-url-loader' },
            {
              loader: 'sass-loader',
              options: {
                sourceMap: true,
                includePaths: [path.resolve(mainDirectory, 'style')]
              }
            }
          ]
        },
        {
          test: /\.css$/,
          use: [
            { loader: 'style-loader' },
            { loader: 'css-loader' }
          ]
        },
        {
          test: /node_modules\/vis\/dist\/img.*$/, // chokes otherwise
          use: [{ loader: 'file-loader' }]
        },
        {
          test: /\.(png|jpg|svg)$/,
          include: [mainDirectory],
          use: [
            {
              loader: 'url-loader',
              options: {
                limit: 16384
              }
            }
          ]
        },
        {
          test: /fonts\/.*$/,
          use: [{ loader: 'file-loader' }]
        },
        {
          test: /octicons.*$/,
          use: [{ loader: 'file-loader' }]
        }
      ]
    },
    plugins: [
      new CaseSensitivePathsPlugin(),
      new webpack.DllReferencePlugin({
        context: '.',
        manifest: isDevelopment
          ? require(path.resolve(dllOutputDirectory, 'dev', manifestFileName))
          : require(path.resolve(dllOutputDirectory, manifestFileName))
      })
    ],
    resolve: {
      modules: [
        mainDirectory,
        path.resolve(mainDirectory, 'view'),
        'node_modules'
      ],
      extensions: ['.js', '.jsx', '.json']
    }
  };

  if (isDevelopmentServer) {
    // don't exit on error
    config.bail = false;

    // cheap-ish source maps that at least give us the right line number
    // for all source map options see: https://webpack.js.org/configuration/devtool/
    config.devtool = '#source-maps';

    // not actually used, but must be set
    config.output.path = path.join(__dirname, 'build/ui-hot');

    // add the necessary plugins
    config.plugins.push(new webpack.HotModuleReplacementPlugin());
    config.plugins.push(new webpack.NoEmitOnErrorsPlugin());
  }

  return config;
}

function createPageEntries(name, isDevelopmentServer) {
  const pagesDirectory = path.join(__dirname, 'src', 'main', 'view', 'pages');

  const pageEntries = ['babel-polyfill', 'whatwg-fetch', path.join(pagesDirectory, name)];

  if (isDevelopmentServer) {
    pageEntries.unshift('webpack-hot-middleware/client');
  }

  return pageEntries;
}

function createJsxLoaders(isDevelopmentServer) {
  const loaders = [];

  if (isDevelopmentServer) {
    loaders.push({ loader: 'react-hot-loader' });
  }

  loaders.push({
    loader: 'babel-loader',
    options: {
      // default cache location is inside node_modules which affects up-to-date checking of Node Gradle tasks
      cacheDirectory: path.resolve(__dirname, 'build', 'babel-cache')
    }
  });

  return loaders;
}
