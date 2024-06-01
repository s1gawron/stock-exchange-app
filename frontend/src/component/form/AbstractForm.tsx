import React, {useState} from 'react';
import styles from "./styles.module.css";
import {FormFieldDTO} from "../../dto/form/FormFieldDTO";
import {Link} from "react-router-dom";
import {FormLinkDTO} from "../../dto/form/FormLinkDTO";

interface FormProps {
    initialValues: any;
    onSubmit: (values: any) => void;
    fields: FormFieldDTO[];
    submitButtonText: string;
    errorMessage?: string;
    formLink: FormLinkDTO;
}

const AbstractForm: React.FC<FormProps> = ({
                                               initialValues,
                                               onSubmit,
                                               fields,
                                               submitButtonText,
                                               errorMessage,
                                               formLink,
                                           }) => {
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

    const renderTextInput = (name: string, type: string, label?: string) => (
        <div key={name}>
            {label && <h4>{label}:</h4>}
            <input
                type={type}
                name={name}
                value={values[name]}
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

    return (
        <div id={styles.formWrapper}>
            <div id={styles.formError}>
                {errorMessage}
            </div>

            <form onSubmit={handleSubmit}>
                {fields.map(({name, type, label, options}) =>
                    type === 'select'
                        ? renderSelectInput(name, label, options)
                        : renderTextInput(name, type, label)
                )}

                <div>
                    <button id={styles.submitBtn} type="submit">
                        {submitButtonText}
                    </button>
                </div>
            </form>

            <Link to={formLink.to}>
                <button className="userLinkBtn">{formLink.text}</button>
            </Link>
        </div>
    );
};

export default AbstractForm;
