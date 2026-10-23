// The land use API client (LANDUSE.01) — thin fetch wrappers, same shape as api.ts.

import { authHeaders, handle, type QueryResult } from './api';

export interface RestrictionType {
  code: string;
  name: string;
}

export interface RestrictionRule {
  id: number;
  restrictionTypeCode: string;
  forbidden: string;
  allowedWithConsent?: string;
  consentParty?: string;
  legalBasis: string;
  validFrom: string;
  validUntil?: string;
  /** Platform columns, filled by the database trigger; read-only. */
  sysStatus?: string;
  sysVersion?: number;
  sysCreatedAt?: string;
  sysCreatedBy?: string;
  sysModifiedAt?: string;
  sysModifiedBy?: string;
}

export type RestrictionRuleRequest = Pick<
  RestrictionRule,
  | 'restrictionTypeCode'
  | 'forbidden'
  | 'allowedWithConsent'
  | 'consentParty'
  | 'legalBasis'
  | 'validFrom'
  | 'validUntil'
>;

export interface PlotRestriction {
  /** Null for a register type this component does not know. */
  restrictionTypeCode: string | null;
  restrictionName: string;
  restrictingObjectOwner?: string;
  legalBasis?: string;
  affectedAreaM2?: number;
  /** The rule valid today for the type; null when there is none. */
  rule: RestrictionRule | null;
}

export interface PlotCheck {
  cadastralCode: string;
  plotAreaM2?: number;
  /** False when the register hides some restrictions — the list is then not the whole truth. */
  registerComplete: boolean;
  restrictions: PlotRestriction[];
}

// A function, not a constant: the signed-in mock user is read per request.
const json = () => ({ ...authHeaders(), 'Content-Type': 'application/json' });

export const landuseApi = {
  /** The cadastral code goes into the path as is (78401:101:0123); 400/404/502 come back as ApiError. */
  check: (cadastralCode: string): Promise<PlotCheck> =>
    fetch(`/api/landuse/plots/${cadastralCode}/restrictions`, { headers: authHeaders() }).then((r) =>
      handle<PlotCheck>(r),
    ),

  types: (): Promise<RestrictionType[]> =>
    fetch('/api/landuse/restriction-types', { headers: authHeaders() }).then((r) =>
      handle<RestrictionType[]>(r),
    ),

  rules: (filter: { typeCode?: string; validOn?: string }): Promise<QueryResult<RestrictionRule>> => {
    const params = new URLSearchParams({ limit: '100', sort: 'restrictionTypeCode' });
    if (filter.typeCode) params.set('typeCode', filter.typeCode);
    if (filter.validOn) params.set('validOn', filter.validOn);
    return fetch(`/api/landuse/rules?${params}`, { headers: authHeaders() }).then((r) =>
      handle<QueryResult<RestrictionRule>>(r),
    );
  },

  rule: (id: number): Promise<RestrictionRule> =>
    fetch(`/api/landuse/rules/${id}`, { headers: authHeaders() }).then((r) => handle<RestrictionRule>(r)),

  /** POST — 201 with the created rule. */
  createRule: (body: RestrictionRuleRequest): Promise<RestrictionRule> =>
    fetch('/api/landuse/rules', { method: 'POST', headers: json(), body: JSON.stringify(body) }).then((r) =>
      handle<RestrictionRule>(r),
    ),

  updateRule: (id: number, body: RestrictionRuleRequest): Promise<RestrictionRule> =>
    fetch(`/api/landuse/rules/${id}`, { method: 'PUT', headers: json(), body: JSON.stringify(body) }).then((r) =>
      handle<RestrictionRule>(r),
    ),

  /** DELETE — a soft delete: 204. */
  retireRule: (id: number): Promise<void> =>
    fetch(`/api/landuse/rules/${id}`, { method: 'DELETE', headers: authHeaders() }).then((r) =>
      r.ok ? undefined : handle<void>(r),
    ),
};
