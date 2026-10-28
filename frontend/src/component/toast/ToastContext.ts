import {createContext, useContext} from "react";

export interface ToastContextValue {
    showError: (msg: string) => void;
}

export const ToastContext = createContext<ToastContextValue | undefined>(undefined);

export function useToast(): ToastContextValue {
    const context = useContext(ToastContext);

    if (context === undefined) {
        throw new Error("useToast must be used within ToastProvider");
    }

    return context;
}
