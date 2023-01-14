import {FormControl, FormGroup, Validators} from "@angular/forms";

export class InstitutionInfoForm extends FormGroup {
  constructor() {
    super({
      name: new FormControl({value:'', disabled: false}, Validators.compose([
        Validators.required,
        Validators.minLength(1)])),
      phone: new FormControl(''),
      email: new FormControl('', Validators.compose([
        Validators.pattern("^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,4}$")
      ]))});
  }

  get name() { return this.controls['name']; }

  get phone() { return this.controls['phone']; }

  get email() { return this.controls['email']; }
}
