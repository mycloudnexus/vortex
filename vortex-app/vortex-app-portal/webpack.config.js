/* eslint-disable @typescript-eslint/no-var-requires */
const path = require('path')
const HtmlWebpackPlugin = require('html-webpack-plugin')
const CopyWebpackPlugin = require('copy-webpack-plugin')
const MiniCssExtractPlugin = require('mini-css-extract-plugin')
const CompressionPlugin = require('compression-webpack-plugin')
const CssMinimizerPlugin = require('css-minimizer-webpack-plugin')
const { CleanWebpackPlugin } = require('clean-webpack-plugin')
const ESLintPlugin = require('eslint-webpack-plugin')
const Dotenv = require('dotenv-webpack')
const BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin
const webpack = require('webpack')
const ModuleFederationPlugin = webpack.container.ModuleFederationPlugin
const { dependencies } = require('./package.json')

/** @type {(env: any, arg: {mode: string}) => import('webpack').Configuration} **/

module.exports = (env, argv) => {
  const isProduction = argv.mode === 'production'
  const isAnalyze = Boolean(env?.analyze)
  /** @type {import('webpack').Configuration} **/
  const config = {
    resolve: {
      extensions: ['.tsx', '.ts', '.jsx', '.js'],
      alias: {
        '@': path.resolve(__dirname, 'src')
      }
    },
    entry: ['./src/index.tsx'],
    module: {
      rules: [
        {
          test: /\.tsx?$/,
          exclude: /node_modules/,
          use: ['babel-loader']
        },
        {
          test: /\.(s[ac]ss|css)$/,
          use: [
            MiniCssExtractPlugin.loader,
            {
              loader: 'css-loader',
              options: { sourceMap: !isProduction }
            },
            {
              loader: 'sass-loader',
              options: { sourceMap: !isProduction }
            }
          ]
        },
        {
          test: /\.(png|jpg|gif)$/,
          use: [
            {
              loader: 'file-loader',
              options: {
                name: isProduction ? 'static/media/[name].[contenthash:6].[ext]' : '[path][name].[ext]'
              }
            }
          ]
        },
        {
          test: /\.svg$/,
          use: [
            {
              loader: require.resolve('@svgr/webpack'),
              options: {
                prettier: false,
                svgo: false,
                svgoConfig: {
                  plugins: [{ removeViewBox: false }]
                },
                titleProp: true,
                ref: true
              }
            },
            {
              loader: require.resolve('file-loader'),
              options: {
                name: 'static/media/[name].[hash].[ext]'
              }
            }
          ],
          issuer: {
            and: [/\.(ts|tsx|js|jsx|md|mdx)$/]
          }
        },
        {
          test: /\.(eot|ttf|woff|woff2)$/,
          use: [
            {
              loader: 'file-loader',
              options: {
                name: isProduction ? 'static/fonts/[name].[ext]' : '[path][name].[ext]'
              }
            }
          ]
        }
      ]
    },
    output: {
      filename: 'static/js/main.[contenthash:6].js',
      path: path.resolve(__dirname, 'dist'),
      publicPath: '/'
    },
    devServer: {
      hot: true,
      port: 3000,
      historyApiFallback: true,
      static: {
        directory: path.resolve(__dirname, 'public', 'index.html'),
        serveIndex: true,
        watch: true
      }
    },
    devtool: isProduction ? false : 'source-map',
    plugins: [
      new MiniCssExtractPlugin({
        filename: isProduction ? 'static/css/[name].[contenthash:6].css' : '[name].css'
      }),
      new Dotenv(),
      new CopyWebpackPlugin({
        patterns: [
          {
            from: 'public',
            to: '.',
            filter: (name) => {
              return !name.endsWith('index.html')
            }
          }
        ]
      }),
      new HtmlWebpackPlugin({
        template: path.resolve(__dirname, 'public', 'index.html'),
        filename: 'index.html'
      }),
      new ESLintPlugin({
        extensions: ['.tsx', '.ts', '.js', '.jsx']
      }),
      new ModuleFederationPlugin({
        name: 'vortex',
        shared: {
          react: {
            singleton: true,
            eager: true,
            requiredVersion: dependencies.react
          },
          'react-dom': {
            singleton: true,
            eager: true,
            requiredVersion: dependencies['react-dom']
          },
          'react-router': {
            singleton: true,
            eager: true,
            requiredVersion: dependencies['react-router']
          },
          'react-router-dom': {
            singleton: true,
            eager: true,
            requiredVersion: dependencies['react-router-dom']
          }
        }
      })
    ]
  }

  if (isProduction) {
    config.plugins = [
      ...config.plugins,
      new webpack.ProgressPlugin(),
      new CompressionPlugin({
        test: /\.(css|js)$/,
        algorithm: 'brotliCompress'
      }),
      new CleanWebpackPlugin()
    ]
    if (isAnalyze) {
      config.plugins = [...config.plugins, new BundleAnalyzerPlugin()]
    }
    config.optimization = {
      minimizer: [`...`, new CssMinimizerPlugin()]
    }
  }
  return config
}
