export default class RedirectUtil {

    static redirectTo(to: string) {
        window.location.assign(to);
    }

}