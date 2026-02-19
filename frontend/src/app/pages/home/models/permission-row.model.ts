import {EmployeeDto} from "../../../shared/dtos/employee-dto.model";
import {InstitutionDto} from "../../../shared/dtos/institution-dto.model";
import {PermissionDto} from "../../../shared/dtos/permission-dto.model";

export type PermissionRow = [InstitutionDto, EmployeeDto, PermissionDto];
