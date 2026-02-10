import {Component} from '@angular/core';
import {DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE, MAT_NATIVE_DATE_FORMATS, NativeDateAdapter} from '@angular/material/core';
import {ServiceNewComponent} from "../service-new/service-new.component";

@Component({
  selector: 'app-service-edit',
  templateUrl: './service-edit.component.html',
  styleUrls: ['./service-edit.component.css'],
  providers: [
    { provide: MAT_DATE_LOCALE, useValue: 'de-DE' },
    {
      provide: DateAdapter,
      useClass: NativeDateAdapter,
      deps: [MAT_DATE_LOCALE],
    },
    { provide: MAT_DATE_FORMATS, useValue: MAT_NATIVE_DATE_FORMATS },
  ],
  standalone: false
})
export class ServiceEditComponent extends ServiceNewComponent {
  pageTitle = 'Eintrag bearbeiten';

  get isLoading(): boolean {
    return this.editMode && !this.clientSelected;
  }

  get clientDisplayName(): string {
    if (!this.selectedClient || !this.selectedClient.id) {
      return '';
    }
    return `${this.selectedClient.lastName} ${this.selectedClient.firstName}`;
  }

  override ngOnInit() {
    super.ngOnInit();
  }

  override initFormSubscriptions() {
    const preservedGoalIds = this.selectedGoals.map(goal => goal.id);
    const valueGoalIds = ((this as any).value?.goals ?? []).map((goal: any) => goal.id);
    const effectiveGoalIds = preservedGoalIds.length > 0 ? preservedGoalIds : valueGoalIds;

    if (this.editMode) {
      const clientValue = this.clientsControl.value;
      if (Array.isArray(clientValue)) {
        const nextClientId = clientValue[0] ?? null;
        this.clientsControl.setValue(nextClientId != null ? Number(nextClientId) : null, { emitEvent: false });
      }

      const planValue = this.assistancePlansControl.value;
      if (Array.isArray(planValue)) {
        const nextPlanId = planValue[0] ?? null;
        this.assistancePlansControl.setValue(nextPlanId != null ? Number(nextPlanId) : null, { emitEvent: false });
      }
    }

    super.initFormSubscriptions();

    if (this.editMode) {
      this.serviceDateControl.disable({ emitEvent: false });

      const planValue = this.assistancePlansControl.value;
      const fallbackPlanId = (this as any).value?.assistancePlanId ?? this.selectedAssistancePlan?.id ?? null;
      const planId = planValue != null ? Number(planValue) : (fallbackPlanId != null ? Number(fallbackPlanId) : null);
      if ((!planValue || Number(planValue) === 0) && fallbackPlanId != null) {
        this.assistancePlansControl.setValue(Number(fallbackPlanId), { emitEvent: false });
      }
      if (planId != null && Number.isFinite(planId) && planId > 0) {
        const plan = this.filteredAssistancePlans.find(value => value.id === planId);
        if (plan) {
          this.selectAssistancePlan(plan);
          this.assistancePlansControl.setValue(planId, { emitEvent: false });
        } else {
          this.assistancePlansControl.setValue(planId, { emitEvent: true });
        }
      } else if ((this.filteredAssistancePlans?.length ?? 0) > 0) {
        const byDate = this.getAssistancePlansByDateString(
          this.filteredAssistancePlans,
          this.selectedServiceDate
        );
        const fallbackPlan = byDate[0] ?? this.filteredAssistancePlans[0];
        if (fallbackPlan) {
          this.selectAssistancePlan(fallbackPlan);
          this.assistancePlansControl.setValue(fallbackPlan.id, { emitEvent: false });
        }
      }

      const finalPlanIdRaw = this.assistancePlansControl.value;
      const finalPlanId = finalPlanIdRaw != null ? Number(finalPlanIdRaw) : null;
      if (finalPlanId != null && Number.isFinite(finalPlanId) && finalPlanId > 0) {
        this.assistancePlansControl.setValue(finalPlanId, { emitEvent: true });
        if (effectiveGoalIds.length > 0) {
          setTimeout(() => {
            this.goalsControl.setValue(effectiveGoalIds, { emitEvent: false });
            this.setGoals(effectiveGoalIds);
          });
        }
      }

      if (effectiveGoalIds.length > 0) {
        this.goalsControl.setValue(effectiveGoalIds, { emitEvent: false });
        this.setGoals(effectiveGoalIds);
      } else if (finalPlanId != null && Number.isFinite(finalPlanId) && finalPlanId > 0) {
        this.betaServiceService.getByAssistancePlan(finalPlanId).subscribe({
          next: (services) => {
            const match = services.find(service => service.id === (this as any).value?.id);
            const matchGoalIds = (match?.goals ?? []).map(goal => goal.id);
            if (matchGoalIds.length > 0) {
              this.goalsControl.setValue(matchGoalIds, { emitEvent: false });
              this.setGoals(matchGoalIds);
              (this as any).value.goals = match?.goals ?? [];
              if ((this as any).value?.assistancePlanId === 0 && match?.assistancePlanId) {
                (this as any).value.assistancePlanId = match.assistancePlanId;
              }
            }
          },
          error: () => {
          }
        });
      }

      const clientValue = this.clientsControl.value;
      const fallbackClientId = this.selectedClient?.id ?? null;
      if ((clientValue == null || Number(clientValue) === 0) && fallbackClientId != null) {
        this.clientsControl.setValue(Number(fallbackClientId), { emitEvent: false });
      }

    }
  }
}
