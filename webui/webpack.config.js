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
            "/react/resv/list": {
                secure: false,
                target: "http://localhost:8181/",
                pathRewrite: { '/react/resv/list': 'index.html' }
            },
            "/react/resv/gui": {
                secure: false,
                target: "http://localhost:8181/",
                pathRewrite: { '/react/resv/gui': 'index.html' }
            },
            "/react/resv/whatif": {
                secure: false,
                target: "http://localhost:8181/",
                pathRewrite: { '/react/resv/whatif': 'index.html' }
            },
            "/resv/*": {
                secure: false,
                target: "https://localhost:8001/"
            },
            "/viz/*": {
                secure: false,
                target: "https://localhost:8001/"
            },
            "/topology/*": {
                secure: false,
                target: "https://localhost:8001/"
            },
            "/info/*": {
                secure: false,
                target: "https://localhost:8001/"
            }
        },
        watchOptions: {
            poll: 1000
        }
    }

};