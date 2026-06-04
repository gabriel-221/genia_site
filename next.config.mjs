/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: false, // VLibras não funciona com StrictMode (duplo useEffect)
  eslint: { ignoreDuringBuilds: true },
  typescript: { ignoreBuildErrors: false },
  images: {
    remotePatterns: [
      { protocol: 'https', hostname: '**' },
    ],
  },
  webpack: (config) => {
    config.resolve.alias['iceberg-js'] = false
    return config
  },
}

export default nextConfig
