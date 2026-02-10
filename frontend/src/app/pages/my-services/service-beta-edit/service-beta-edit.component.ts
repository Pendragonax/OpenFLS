import {Component} from '@angular/core';
import {DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE, MAT_NATIVE_DATE_FORMATS, NativeDateAdapter} from '@angular/material/core';
import {ServiceBetaNewComponent} from "../service-beta-new/service-beta-new.component";

@Component({
  selector: 'app-service-beta-edit',
  templateUrl: './service-beta-edit.component.html',
  styleUrls: ['./service-beta-edit.component.css'],
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
export class ServiceBetaEditComponent extends ServiceBetaNewComponent {
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
      console.log('[service-beta-edit] initFormSubscriptions editMode start', {
        clientSelected: this.clientSelected,
        selectedClientId: this.selectedClient?.id,
        assistancePlanSelected: this.assistancePlanSelected,
        selectedAssistancePlanId: this.selectedAssistancePlan?.id,
        valueAssistancePlanId: (this as any).value?.assistancePlanId,
        preservedGoalIds,
        valueGoalIds,
        clientsControlValue: this.clientsControl.value,
        assistancePlansControlValue: this.assistancePlansControl.value,
        filteredAssistancePlans: this.filteredAssistancePlans?.length ?? 0,
        effectiveGoalIds
      });

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

      console.log('[service-beta-edit] initFormSubscriptions normalized', {
        clientsControlValue: this.clientsControl.value,
        assistancePlansControlValue: this.assistancePlansControl.value
      });
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
      console.log('[service-beta-edit] post-initFormSubscriptions plan check', {
        planValue,
        planId,
        filteredAssistancePlans: this.filteredAssistancePlans?.length ?? 0,
        assistancePlanSelected: this.assistancePlanSelected
      });
      if (planId != null && Number.isFinite(planId) && planId > 0) {
        const plan = this.filteredAssistancePlans.find(value => value.id === planId);
        if (plan) {
          this.selectAssistancePlan(plan);
          this.assistancePlansControl.setValue(planId, { emitEvent: false });
          console.log('[service-beta-edit] plan selected from filtered list', {
            planId,
            assistancePlanSelected: this.assistancePlanSelected
          });
        } else {
          this.assistancePlansControl.setValue(planId, { emitEvent: true });
          console.log('[service-beta-edit] plan not found in filtered list, emitted value', {
            planId
          });
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
          console.log('[service-beta-edit] fallback plan selected', {
            planId: fallbackPlan.id,
            assistancePlanSelected: this.assistancePlanSelected
          });
        }
      }

      const finalPlanIdRaw = this.assistancePlansControl.value;
      const finalPlanId = finalPlanIdRaw != null ? Number(finalPlanIdRaw) : null;

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
              console.log('[service-beta-edit] goals loaded from assistance plan services', {
                planId: finalPlanId,
                serviceId: match?.id,
                matchGoalIds
              });
            } else {
              console.log('[service-beta-edit] no goals found in assistance plan services', {
                planId: finalPlanId,
                serviceId: (this as any).value?.id
              });
            }
          },
          error: () => {
            console.log('[service-beta-edit] failed to load goals from assistance plan services', {
              planId: finalPlanId
            });
          }
        });
      } else {
        console.log('[service-beta-edit] skipped goal fetch due to missing plan id', {
          finalPlanId,
          planId
        });
      }

      const clientValue = this.clientsControl.value;
      const fallbackClientId = this.selectedClient?.id ?? null;
      if ((clientValue == null || Number(clientValue) === 0) && fallbackClientId != null) {
        this.clientsControl.setValue(Number(fallbackClientId), { emitEvent: false });
      }

      console.log('[service-beta-edit] initFormSubscriptions editMode end', {
        clientSelected: this.clientSelected,
        selectedClientId: this.selectedClient?.id,
        assistancePlanSelected: this.assistancePlanSelected,
        selectedAssistancePlanId: this.selectedAssistancePlan?.id,
        filteredAssistancePlans: this.filteredAssistancePlans?.length ?? 0,
        assistancePlansControlValue: this.assistancePlansControl.value,
        selectedGoals: this.selectedGoals.map(goal => goal.id),
        goalsControlValue: this.goalsControl.value
      });
    }
  }
}
