import React, {useState} from 'react';
import styles from "./styles.module.css";
import {FormLinkDTO} from "../../dto/form/FormLinkDTO.ts";
import LinkButton from "../linkButton/LinkButton.tsx";

interface FormFieldOptionsDTO {
    value: any;
    label: string;
}

interface FormFieldDTO {
    name: string;
    type: string;
    value?: any;
    hidden?: boolean;
    label?: string;
    options?: FormFieldOptionsDTO[];
}

interface FormProps {
    initialValues: any;
    onSubmit: (values: any) => void;
    fields: FormFieldDTO[];
    submitButtonText: string;
    formLink?: FormLinkDTO;
}

const AbstractForm: React.FC<FormProps> = ({
                                               initialValues,
                                               onSubmit,
                                               fields,
                                               submitButtonText,
                                               formLink,
                                           }: FormProps) => {
    const [values, setValues] = useState(initialValues);

    const handleChange = (
        e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
    ) => {
        const {name, value} = e.target;
        setValues((prevValues: typeof initialValues) => ({
            ...prevValues,
            [name]: value,
        }));
    };

    const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        onSubmit(values);
    };

    const renderDefaultInput = (name: string, type: string, value?: any, hidden?: boolean, label?: string) => (
        <div key={name}>
            {label && <h4>{label}:</h4>}
            <input
                type={type}
                name={name}
                hidden={hidden === undefined ? false : hidden}
                value={value === undefined ? values[name] : value}
                onChange={handleChange}
                className={styles.textInput}
                required
            />
        </div>
    );

    const renderSelectInput = (
        name: string,
        label?: string,
        options?: { value: any; label: string }[]
    ) => (
        <div key={name}>
            {<h4>{label}:</h4>}
            <select
                name={name}
                value={values[name]}
                onChange={handleChange}
                className={styles.selectInput}
            >
                {options?.map(({value, label}) => (
                    <option key={value} value={value}>
                        {label}
                    </option>
                ))}
            </select>
        </div>
    );

    const renderRadioInput = (
        name: string,
        label?: string,
        options?: { value: any; label: string }[]
    ) => (
        <div key={name}>
            {label && <h4>{label}:</h4>}
            {options?.map(({value, label}) => (
                <div key={value} className={styles.radioOption}>
                    <input
                        id={value}
                        type="radio"
                        name={name}
                        value={value}
                        onChange={handleChange}
                    />
                    <label htmlFor={value}>{label}</label>
                </div>
            ))}
        </div>
    );

    return (
        <div id={styles.formWrapper}>
            <form onSubmit={handleSubmit}>
                {fields.map(({name, type, value, hidden, label, options}: FormFieldDTO) => {
                    switch (type) {
                        case "select":
                            return renderSelectInput(name, label, options);
                        case "radio":
                            return renderRadioInput(name, label, options);
                        default:
                            return renderDefaultInput(name, type, value, hidden, label);
                    }
                })}

                <div>
                    <button id={styles.submitBtn} type="submit">
                        {submitButtonText}
                    </button>
                </div>
            </form>

            {formLink ? (
                <div id={styles.linkBtnWrapper}>
                    <LinkButton props={{linkTo: formLink.to, text: formLink.text}}/>
                </div>
            ) : (<></>)}
        </div>
    );
};

export default AbstractForm;
