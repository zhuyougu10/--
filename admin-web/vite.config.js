import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'
import fs from 'fs'

export default defineConfig(({ mode }) => {
  const enableHttps = process.env.VITE_HTTPS === '1' || mode === 'https'
  const certDir = path.resolve(__dirname, '.cert')
  const keyPath = path.resolve(certDir, 'dev-key.pem')
  const certPath = path.resolve(certDir, 'dev-cert.pem')

  const hasCustomCert = fs.existsSync(keyPath) && fs.existsSync(certPath)
  const httpsConfig = enableHttps
    ? (hasCustomCert
      ? {
          key: fs.readFileSync(keyPath),
          cert: fs.readFileSync(certPath)
        }
      : true)
    : false

  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': path.resolve(__dirname, 'src')
      }
    },
    server: {
      host: '0.0.0.0',
      port: 3000,
      strictPort: true,
      https: httpsConfig,
      proxy: {
        '/api': {
          target: 'http://127.0.0.1:8080',
          changeOrigin: true
        }
      }
    }
  }
})
