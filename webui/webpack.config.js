var path = require('path');

module.exports = {
    entry: './src/main/js/app.js',
    devtool: 'sourcemaps',
    cache: true,
    debug: true,
    output: {
        path:  path.join(__dirname, 'src', 'main', 'resources', 'static', 'built'),
        filename: 'bundle.js'
    },
    module: {
        loaders: [
            {
                test: /\.js|\.jsx/,
                exclude: /(node_modules)/,
                loader: 'babel',
                query: {
                    cacheDirectory: true,
                    presets: ['es2015', 'react']
                }
            }
        ]
    },
    devServer: {
        port: 8181,
        contentBase: 'src/main/resources/static/built/',
        proxy: {
            "/react": {
                secure: false,
                target: "http://localhost:8181/",
                pathRewrite: { '/react': 'index.html' }
            },
            "/resv/*": {
                secure: false,
                target: "https://localhost:8001/"
            },
            "/viz/*": {
                secure: false,
                target: "https://localhost:8001/"
            }
        },
        watchOptions: {
            poll: 1000
        }
    }

};