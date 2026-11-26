import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { DatePicker, Form } from 'antd';
import dayjs from 'dayjs';
import { FileProtectOutlined } from '@ant-design/icons';
import { useDataController } from '@helex/core';
import {
  AppCard,
  AppDate,
  AppTag,
  AppTimestamp,
  FieldItem,
  ResourceForm,
  useAppNotification,
  type ResourceFormSection,
} from '@helex/ui';
import { landuseApi, type RestrictionRule, type RestrictionType } from '../landuseApi';

/**
 * The string ↔ dayjs date input from AnimalDetail.tsx (see the note there for why
 * ResourceForm's built-in `date` field cannot be used), without the "not in the
 * future" limit — a rule may start or end on any day.
 */
const DateField = ({ value, onChange }: { value?: unknown; onChange?: (next: string | null) => void }) => (
  <DatePicker
    style={{ width: '100%' }}
    value={value ? dayjs(String(value)) : null}
    onChange={(d) => onChange?.(d ? d.format('YYYY-MM-DD') : null)}
  />
);

/**
 * One rule of the catalogue, on ResourceForm — create at /landuse/rules/new,
 * view / edit / retire at /landuse/rules/{id}, as AnimalDetail. Save is POST
 * (201) or PUT; Retire is the soft DELETE (204). A 400 problem detail is shown
 * verbatim.
 */
export const RuleDetail = () => {
  const { id } = useParams();
  const isNew = id === 'new';
  const ruleId = Number(id);
  const navigate = useNavigate();
  const notify = useAppNotification();
  const [form] = Form.useForm<RestrictionRule>();
  const dc = useDataController<RestrictionRule>({} as RestrictionRule);
  const [mode, setMode] = useState<'create' | 'view' | 'edit'>(isNew ? 'create' : 'view');
  const [loading, setLoading] = useState(!isNew);
  const [saving, setSaving] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [types, setTypes] = useState<RestrictionType[]>([]);

  useEffect(() => {
    let cancelled = false;
    landuseApi.types().then((list) => !cancelled && setTypes(list)).catch((e) => notify.error('Could not load restriction types', e.message));
    if (isNew) {
      dc.load({} as RestrictionRule);
      form.resetFields();
      setMode('create');
      setLoading(false);
    } else {
      setLoading(true);
      setMode('view');
      landuseApi
        .rule(ruleId)
        .then((rule) => {
          if (cancelled) return;
          dc.load(rule);
          form.setFieldsValue(rule);
        })
        .catch((e) => {
          notify.error('Could not load the rule', e.message);
          navigate('/landuse/rules');
        })
        .finally(() => !cancelled && setLoading(false));
    }
    return () => {
      cancelled = true;
    };
    // dc, form and navigate are stable handles; the record is keyed by the route parameter.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id, notify]);

  const typeName = (code?: string) => types.find((t) => t.code === code)?.name ?? code;

  const save = async (values: RestrictionRule) => {
    const body = {
      restrictionTypeCode: values.restrictionTypeCode,
      forbidden: values.forbidden,
      allowedWithConsent: values.allowedWithConsent || undefined,
      consentParty: values.consentParty || undefined,
      legalBasis: values.legalBasis,
      validFrom: values.validFrom,
      validUntil: values.validUntil || undefined,
    };
    setSaving(true);
    try {
      if (isNew) {
        const created = await landuseApi.createRule(body);
        notify.success('Rule added', typeName(created.restrictionTypeCode));
        navigate(`/landuse/rules/${created.id}`, { replace: true });
      } else {
        const updated = await landuseApi.updateRule(ruleId, body);
        dc.load(updated);
        form.setFieldsValue(updated);
        setMode('view');
        notify.success('Saved', typeName(updated.restrictionTypeCode));
      }
    } catch (e) {
      notify.error('Save failed', (e as Error).message); // 400 problem detail, verbatim
    } finally {
      setSaving(false);
    }
  };

  const retire = async () => {
    setDeleting(true);
    try {
      await landuseApi.retireRule(ruleId);
      notify.success('Retired', typeName(dc.current.restrictionTypeCode));
      navigate('/landuse/rules');
    } catch (e) {
      notify.error('Retire failed', (e as Error).message);
      setDeleting(false);
    }
  };

  const sections: ResourceFormSection<RestrictionRule>[] = [
    {
      key: 'rule',
      title: 'Rule',
      fields: [
        {
          name: 'restrictionTypeCode',
          label: 'Restriction type',
          type: 'select',
          required: true,
          rules: [{ required: true }],
          options: types.map((t) => ({ value: t.code, label: t.name })),
          viewRender: (code) => <AppTag>{typeName(String(code ?? ''))}</AppTag>,
        },
        {
          name: 'forbidden',
          label: 'Forbidden',
          type: 'textarea',
          required: true,
          rules: [{ required: true, max: 2000 }],
          colSpan: 24,
        },
        {
          name: 'allowedWithConsent',
          label: 'Allowed with consent',
          type: 'textarea',
          rules: [{ max: 2000 }],
          colSpan: 24,
        },
        { name: 'consentParty', label: 'Consent party', type: 'text', rules: [{ max: 255 }] },
        {
          name: 'legalBasis',
          label: 'Legal basis',
          type: 'text',
          required: true,
          rules: [{ required: true, max: 500 }],
        },
      ],
    },
    {
      key: 'validity',
      title: 'Validity',
      fields: [
        {
          name: 'validFrom',
          label: 'Valid from',
          type: 'custom',
          required: true,
          rules: [{ required: true, message: 'Valid from is required' }],
          render: ({ value, onChange }) => <DateField value={value} onChange={onChange} />,
          viewRender: (value) => (value ? <AppDate value={String(value)} /> : '—'),
        },
        {
          name: 'validUntil',
          label: 'Valid until',
          type: 'custom',
          tooltip: 'Empty while the rule has no end; not before valid from',
          rules: [
            ({ getFieldValue }: { getFieldValue: (name: string) => unknown }) => ({
              validator: (_: unknown, until?: string) => {
                const from = getFieldValue('validFrom');
                return until && from && String(until) < String(from)
                  ? Promise.reject(new Error('valid until must not be before valid from'))
                  : Promise.resolve();
              },
            }),
          ],
          render: ({ value, onChange }) => <DateField value={value} onChange={onChange} />,
          viewRender: (value) => (value ? <AppDate value={String(value)} /> : '—'),
        },
      ],
    },
  ];

  return (
    <ResourceForm<RestrictionRule>
      mode={mode}
      entityLabel="Restriction rule"
      createTitle="New restriction rule"
      icon={<FileProtectOutlined />}
      name={isNew ? undefined : typeName(dc.current.restrictionTypeCode)}
      subtitle={isNew ? undefined : dc.current.legalBasis}
      loading={loading}
      saving={saving}
      deleting={deleting}
      form={form}
      dataController={dc}
      initialValues={dc.current}
      sections={sections}
      onBack={() => navigate('/landuse/rules')}
      backTooltip="Back to the list"
      onEdit={() => setMode('edit')}
      onCancel={() => {
        if (isNew) {
          navigate('/landuse/rules');
          return;
        }
        dc.reset();
        form.setFieldsValue(dc.original ?? dc.current);
        setMode('view');
      }}
      onSave={save}
      onDelete={isNew ? undefined : retire}
      deleteLabel="Retire"
      deleteConfirmText="Retire this rule? A soft delete — checks will no longer show it."
      sidebar={
        isNew ? undefined : (
          <AppCard title="Metadata" size="small">
            <FieldItem label="Created at">
              <AppTimestamp value={dc.current.sysCreatedAt} fallback="—" />
            </FieldItem>
            <FieldItem label="Created by">{dc.current.sysCreatedBy ?? '—'}</FieldItem>
            <FieldItem label="Modified at">
              <AppTimestamp value={dc.current.sysModifiedAt} fallback="—" />
            </FieldItem>
            <FieldItem label="Modified by">{dc.current.sysModifiedBy ?? '—'}</FieldItem>
            <FieldItem label="Version">{dc.current.sysVersion ?? '—'}</FieldItem>
          </AppCard>
        )
      }
    />
  );
};
