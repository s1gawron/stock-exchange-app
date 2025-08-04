/// <reference types="vite/client" />

interface ImportMetaEnv {
    readonly VITE_API_SERVICE_SCHEME: string
    readonly VITE_API_SERVICE_HOST: string
    readonly VITE_API_SERVICE_PORT?: string
}

interface ImportMeta {
    readonly env: ImportMetaEnv
}
