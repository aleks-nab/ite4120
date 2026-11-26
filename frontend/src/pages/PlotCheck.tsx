import { useEffect, useState } from 'react';
import { Alert, Form, Input, Table, type TableColumnsType } from 'antd';
import {
  AppButtonPrimary,
  AppButtonSecondary,
  AppCard,
  AppTag,
  FieldItem,
  useAppNotification,
} from '@helex/ui';
import { ApiError } from '../api';
import { landuseApi, type PlotCheck as PlotCheckResult, type PlotRestriction, type RestrictionType } from '../landuseApi';

const CADASTRAL_FORMAT = /^\d{5}:\d{3}:\d{4}$/;

const area = (m2?: number) => (m2 == null ? '—' : `${m2.toLocaleString()} m²`);

/**
 * The plot check — a landowner enters a cadastral number and sees the
 * restrictions on the plot, each explained by the rule valid today. Field by
 * field this follows docs/specifications/LANDUSE.01 § UI. Fails closed: when the
 * register is down (502) the page shows the error and a retry, never a partial list.
 */
export const PlotCheck = () => {
  const notify = useAppNotification();
  const [types, setTypes] = useState<RestrictionType[]>([]);
  const [checking, setChecking] = useState(false);
  const [result, setResult] = useState<PlotCheckResult | null>(null);
  const [registerDown, setRegisterDown] = useState<string | null>(null);

  useEffect(() => {
    landuseApi.types().then(setTypes).catch((e) => notify.error('Could not load restriction types', e.message));
  }, [notify]);

  const check = (cadastralCode: string) => {
    setChecking(true);
    setResult(null);
    setRegisterDown(null);
    landuseApi
      .check(cadastralCode)
      .then(setResult)
      .catch((e) => {
        if (e instanceof ApiError && e.status === 502) setRegisterDown(cadastralCode);
        else notify.error('Check failed', e.message); // 400/404 problem detail, verbatim
      })
      .finally(() => setChecking(false));
  };

  const typeName = (code: string | null) => types.find((t) => t.code === code)?.name ?? code;

  const columns: TableColumnsType<PlotRestriction> = [
    {
      key: 'type',
      title: 'Type',
      dataIndex: 'restrictionTypeCode',
      render: (code: string | null) => (code ? <AppTag>{typeName(code)}</AppTag> : '—'),
    },
    { key: 'name', title: 'Name', dataIndex: 'restrictionName' },
    {
      key: 'owner',
      title: 'Owner of the restricting object',
      dataIndex: 'restrictingObjectOwner',
      render: (v?: string) => v || '—',
    },
    { key: 'legalBasis', title: 'Legal basis', dataIndex: 'legalBasis', render: (v?: string) => v || '—' },
    {
      key: 'area',
      title: 'Affected area',
      dataIndex: 'affectedAreaM2',
      align: 'right',
      render: (m2?: number) => area(m2),
    },
    {
      key: 'share',
      title: '% of the plot',
      align: 'right',
      render: (_, r) =>
        r.affectedAreaM2 != null && result?.plotAreaM2
          ? `${((r.affectedAreaM2 / result.plotAreaM2) * 100).toFixed(1)} %`
          : '—',
    },
  ];

  const explanation = (r: PlotRestriction) =>
    r.rule ? (
      <div style={{ display: 'grid', gap: 4 }}>
        <FieldItem label="Forbidden">{r.rule.forbidden}</FieldItem>
        <FieldItem label="Allowed with consent">{r.rule.allowedWithConsent || '—'}</FieldItem>
        <FieldItem label="Consent party">{r.rule.consentParty || '—'}</FieldItem>
        <FieldItem label="Legal basis">{r.rule.legalBasis}</FieldItem>
      </div>
    ) : (
      <div>
        Explanation not available
        {r.legalBasis && <div style={{ opacity: 0.65 }}>Register's legal basis: {r.legalBasis}</div>}
      </div>
    );

  return (
    <div style={{ maxWidth: 1100, margin: '24px auto', display: 'grid', gap: 16 }}>
      <AppCard title="Check a plot">
        <Form layout="inline" onFinish={(v: { cadastralCode: string }) => check(v.cadastralCode.trim())} requiredMark>
          <Form.Item
            name="cadastralCode"
            label="Cadastral number"
            rules={[
              { required: true, message: 'Cadastral number is required' },
              { pattern: CADASTRAL_FORMAT, message: 'cadastral number must have the format NNNNN:NNN:NNNN' },
            ]}
            extra="e.g. 78401:101:0123"
          >
            <Input placeholder="78401:101:0123" style={{ width: 220 }} />
          </Form.Item>
          <Form.Item>
            <AppButtonPrimary htmlType="submit" loading={checking}>
              Check
            </AppButtonPrimary>
          </Form.Item>
        </Form>
      </AppCard>

      {registerDown && (
        <Alert
          type="error"
          showIcon
          title="Register unavailable, try again"
          action={
            <AppButtonSecondary onClick={() => check(registerDown)} loading={checking}>
              Retry
            </AppButtonSecondary>
          }
        />
      )}

      {result && (
        <>
          <AppCard title="Plot" size="small">
            <FieldItem label="Cadastral number">{result.cadastralCode}</FieldItem>
            <FieldItem label="Plot area">{area(result.plotAreaM2)}</FieldItem>
          </AppCard>
          {!result.registerComplete && (
            <Alert
              type="warning"
              showIcon
              title="The register does not disclose all restrictions on this plot — this list may be incomplete."
            />
          )}
          <Table<PlotRestriction>
            columns={columns}
            dataSource={result.restrictions}
            rowKey={(r) => `${r.restrictionTypeCode ?? ''}|${r.restrictionName}|${r.restrictingObjectOwner ?? ''}`}
            pagination={false}
            expandable={{ expandedRowRender: explanation }}
            locale={{ emptyText: 'No restrictions on this plot' }}
          />
        </>
      )}
    </div>
  );
};
