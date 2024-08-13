import {ReactElement} from "react";

export default interface LinkButtonProperties {
    linkTo: string;
    icon?: ReactElement;
    text: string;
    fontSize?: string;
    onBtnClick?: () => void;
}