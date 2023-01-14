import {FormControl, FormGroup, Validators} from "@angular/forms";

export class AssistancePlanInfoForm extends FormGroup {
  constructor(withoutInstitution: boolean = false) {
    super({
      start: new FormControl(Date.now(), Validators.compose([Validators.required])),
      end: new FormControl(Date.now(), Validators.compose([Validators.required])),
      sponsor: new FormControl(null, Validators.compose([Validators.required])),
      institution: new FormControl(
        null,
        withoutInstitution ? null : Validators.compose([Validators.required]))
    });
  }

  get start() { return this.controls['start']; }

  get end() { return this.controls['end']; }

  get sponsor() { return this.controls['sponsor']; }

  get institution() { return this.controls['institution']; }
}
