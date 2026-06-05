/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: false, // VLibras não funciona com StrictMode (duplo useEffect)
  eslint: { ignoreDuringBuilds: false },
  typescript: { ignoreBuildErrors: false },
  images: {
    remotePatterns: [
      { protocol: 'https', hostname: '*.supabase.co' },
    ],
  },
  webpack: (config) => {
    config.resolve.alias['iceberg-js'] = false
    return config
  },
}

export default nextConfig
