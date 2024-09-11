import { Injectable } from '@angular/core';
import {EmployeeDto} from "../dtos/employee-dto.model";
import {PermissionDto} from "../dtos/permission-dto.model";
import {InstitutionDto} from "../dtos/institution-dto.model";
import {UnprofessionalDto} from "../dtos/unprofessional-dto.model";
import {SponsorDto} from "../dtos/sponsor-dto.model";

@Injectable({
  providedIn: 'root'
})
export class DtoCombinerService {

  constructor() { }

  combineNotProfessionals(
    notProfessionals: UnprofessionalDto[],
    employees: EmployeeDto[],
    sponsors: SponsorDto[]
  ): [EmployeeDto, SponsorDto, UnprofessionalDto][] {
    return notProfessionals.map(unprofessional => {
      return [
        employees.find(value => value.id == unprofessional.employeeId) ?? new EmployeeDto(),
        sponsors.find(value => value.id == unprofessional.sponsorId) ?? new SponsorDto(),
        unprofessional
      ]
    });
  }

  combinePermissionsByEmployee(
    employee: EmployeeDto,
    institutions: InstitutionDto[]
  ): [InstitutionDto, EmployeeDto, PermissionDto][] {
    return institutions.map(institution => {
      if (employee.permissions?.some(permission => permission.institutionId == institution.id)) {
        const permission = employee.permissions.find(val => val.institutionId == institution.id)

        if (permission !== undefined) {
          return [
            institution,
            employee,
            <PermissionDto> {
              employeeId: employee.id,
              institutionId: institution.id,
              writeEntries: permission.writeEntries,
              readEntries: permission.readEntries,
              changeInstitution: permission.changeInstitution,
              affiliated: permission.affiliated }
          ]
        }
      }
      return [
        institution,
        employee,
        <PermissionDto> {
          employeeId: employee.id,
          institutionId: institution.id,
          writeEntries: false,
          readEntries: false,
          changeInstitution: false,
          affiliated: false }
      ]
    });
  }

  combinePermissionsByInstitution(
    institution: InstitutionDto,
    employees: EmployeeDto[]
  ): [InstitutionDto, EmployeeDto, PermissionDto][] {
    return employees.map(employee => {
      if (institution.permissions?.some(permission => permission.employeeId == employee.id)) {
        const permission = institution.permissions.find(val => val.employeeId == employee.id)

        if (permission !== undefined) {
          return [
            institution,
            employee,
            <PermissionDto> {
              employeeId: employee.id,
              institutionId: institution.id,
              writeEntries: permission.writeEntries,
              readEntries: permission.readEntries,
              changeInstitution: permission.changeInstitution,
              affiliated: permission.affiliated }
          ]
        }
      }
      return [
        institution,
        employee,
        <PermissionDto> {
          employeeId: employee.id,
          institutionId: institution.id,
          writeEntries: false,
          readEntries: false,
          changeInstitution: false,
          affiliated: false }
      ]
    });
  }
}
