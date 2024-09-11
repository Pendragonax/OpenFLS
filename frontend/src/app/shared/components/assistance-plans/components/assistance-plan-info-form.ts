import {UntypedFormControl, UntypedFormGroup, Validators} from "@angular/forms";

export class AssistancePlanInfoForm extends UntypedFormGroup {
  constructor(withoutInstitution: boolean = false) {
    super({
      start: new UntypedFormControl(Date.now(), Validators.compose([Validators.required])),
      end: new UntypedFormControl(Date.now(), Validators.compose([Validators.required])),
      sponsor: new UntypedFormControl(null, Validators.compose([Validators.required])),
      institution: new UntypedFormControl(
        null,
        withoutInstitution ? null : Validators.compose([Validators.required]))
    });
  }

  get start() { return this.controls['start']; }

  get end() { return this.controls['end']; }

  get sponsor() { return this.controls['sponsor']; }

  get institution() { return this.controls['institution']; }
}
