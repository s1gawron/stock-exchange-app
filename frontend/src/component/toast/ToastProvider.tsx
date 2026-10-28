import React, {useCallback, useEffect, useMemo, useRef, useState} from "react";
import styles from "./styles.module.css";
import {ToastContext} from "./ToastContext.ts";
import Toast from "./Toast.tsx";

const TOAST_DURATION_MS: number = 5000;

interface ToastMessage {
    id: number;
    msg: string;
}

export default function ToastProvider({children}: { children: React.ReactNode }): React.ReactElement {
    const [toasts, setToasts] = useState<ToastMessage[]>([]);
    const nextIdRef = useRef<number>(0);
    const timersRef = useRef<Map<number, ReturnType<typeof setTimeout>>>(new Map());

    const closeToast = useCallback((id: number) => {
        clearTimeout(timersRef.current.get(id));
        timersRef.current.delete(id);
        setToasts(prev => prev.filter(toast => toast.id !== id));
    }, []);

    const showError = useCallback((msg: string) => {
        const id = nextIdRef.current++;

        // The same message is not shown twice at once, e.g. when several requests fail for the same reason
        setToasts(prev => prev.some(toast => toast.msg === msg) ? prev : [...prev, {id, msg}]);
        timersRef.current.set(id, setTimeout(() => closeToast(id), TOAST_DURATION_MS));
    }, [closeToast]);

    useEffect(() => {
        const timers = timersRef.current;
        return () => timers.forEach(timer => clearTimeout(timer));
    }, []);

    const contextValue = useMemo(() => ({showError}), [showError]);

    return (
        <ToastContext.Provider value={contextValue}>
            {children}

            <div id={styles.toastContainer}>
                {toasts.map(toast => (
                    <Toast key={toast.id} msg={toast.msg} onClose={() => closeToast(toast.id)}/>
                ))}
            </div>
        </ToastContext.Provider>
    );
}
