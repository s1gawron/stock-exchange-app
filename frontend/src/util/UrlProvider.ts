export function buildUrl(suffix: string, version: string, path: string): string {
    let url: string = import.meta.env.VITE_API_SERVICE_SCHEME + "://" + import.meta.env.VITE_API_SERVICE_HOST;
    const port: string | undefined = import.meta.env.VITE_API_SERVICE_PORT;

    if (port !== undefined) {
        url = url + ":" + port;
    }

    return url + "/" + suffix + "/" + version + "/" + path;
}
