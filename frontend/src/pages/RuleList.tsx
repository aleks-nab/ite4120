import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { DatePicker, Form, Select } from 'antd';
import type { Dayjs } from 'dayjs';
import { PlusOutlined } from '@ant-design/icons';
import {
  AppButtonPrimary,
  AppLink,
  ResourceList,
  useAppNotification,
  type ResourceListColumn,
} from '@helex/ui';
import { landuseApi, type RestrictionRule, type RestrictionType } from '../landuseApi';

/**
 * The rule catalogue — on ResourceList, as AnimalList. Filter by restriction
 * type and by the day a rule is valid on (both server-side); the type links to
 * the rule's page.
 */
export const RuleList = () => {
  const navigate = useNavigate();
  const notify = useAppNotification();
  const [rules, setRules] = useState<RestrictionRule[]>([]);
  const [types, setTypes] = useState<RestrictionType[]>([]);
  const [loading, setLoading] = useState(true);
  const [typeCode, setTypeCode] = useState<string | undefined>();
  const [validOn, setValidOn] = useState<Dayjs | null>(null);

  const load = useCallback(
    (filter: { typeCode?: string; validOn?: string }) => {
      setLoading(true);
      landuseApi
        .rules(filter)
        .then((result) => setRules(result.data))
        .catch((e) => notify.error('Could not load rules', e.message))
        .finally(() => setLoading(false));
    },
    [notify],
  );

  useEffect(() => {
    landuseApi.types().then(setTypes).catch((e) => notify.error('Could not load restriction types', e.message));
  }, [notify]);

  useEffect(() => {
    load({ typeCode, validOn: validOn?.format('YYYY-MM-DD') });
  }, [typeCode, validOn, load]);

  const typeName = (code: string) => types.find((t) => t.code === code)?.name ?? code;

  const columns: ResourceListColumn<RestrictionRule>[] = [
    {
      key: 'restrictionTypeCode',
      title: 'Type',
      dataIndex: 'restrictionTypeCode',
      locked: true,
      render: (code: string, rule: RestrictionRule) => (
        <AppLink
          href={`/landuse/rules/${rule.id}`}
          onClick={(e) => {
            e.preventDefault();
            e.stopPropagation();
            navigate(`/landuse/rules/${rule.id}`);
          }}
        >
          {typeName(code)}
        </AppLink>
      ),
    },
    { key: 'forbidden', title: 'Forbidden', dataIndex: 'forbidden', ellipsis: true },
    {
      key: 'consentParty',
      title: 'Consent party',
      dataIndex: 'consentParty',
      render: (v?: string) => v || '—',
    },
    { key: 'validFrom', title: 'Valid from', dataIndex: 'validFrom' },
    { key: 'validUntil', title: 'Valid until', dataIndex: 'validUntil', render: (v?: string) => v || '—' },
  ];

  return (
    <ResourceList<RestrictionRule>
      title="Restriction rules"
      columns={columns}
      dataSource={rules}
      rowKey="id"
      loading={loading}
      filter={{
        defaultOpen: true,
        onClear: () => {
          setTypeCode(undefined);
          setValidOn(null);
        },
        content: (
          <Form layout="vertical">
            <Form.Item label="Restriction type">
              <Select
                allowClear
                value={typeCode}
                onChange={setTypeCode}
                options={types.map((t) => ({ value: t.code, label: t.name }))}
                placeholder="Any type"
              />
            </Form.Item>
            <Form.Item label="Valid on">
              <DatePicker style={{ width: '100%' }} value={validOn} onChange={setValidOn} />
            </Form.Item>
          </Form>
        ),
      }}
      actions={
        <AppButtonPrimary icon={<PlusOutlined />} onClick={() => navigate('/landuse/rules/new')}>
          Add rule
        </AppButtonPrimary>
      }
    />
  );
};
