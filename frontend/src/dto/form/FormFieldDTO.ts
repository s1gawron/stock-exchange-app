import {FormFieldOptionsDTO} from "./FormFieldOptionDTO";

export interface FormFieldDTO {
    name: string;
    type: string;
    label?: string;
    options?: FormFieldOptionsDTO[];
}
