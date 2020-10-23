const { merge } = require('webpack-merge');
const generated = require('./scalajs.webpack.config');

const local = {
    module: {
        rules: [
            {
                test: /\.css$/,
                use: ['style-loader', 'css-loader']
            },
            {
                test: /\.(ttf|eot|woff2?|png|glb)$/,
                use: 'url-loader'
            },
            {
                test: /\.(eot)$/,
                use: 'url-loader'
            }
        ]
    }
};

module.exports = merge(generated, local);
