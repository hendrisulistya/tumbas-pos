// Webpack custom configuration for production hashing
if (config.mode === 'production') {
    config.output = config.output || {};
    config.output.filename = (chunkData) => {
        return chunkData.chunk.name === 'main'
            ? 'composeApp.[contenthash].js'
            : 'composeApp-[name].[contenthash].js';
    };
}
