import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { DatePicker, Form, Table, Typography } from 'antd';
import dayjs, { type Dayjs } from 'dayjs';
import { AppButtonSecondary, AppCard, useAppNotification } from '@helex/ui';
import { landuseApi, type RestrictionSummaryRow } from '../landuseApi';

/**
 * The coverage report — one row per restriction type: is a rule in force today,
 * whose consent it names, and how many pre-checks in the period showed the type.
 * Opened from the Restriction rules page.
 */
export const CoverageReport = () => {
  const navigate = useNavigate();
  const notify = useAppNotification();
  const [period, setPeriod] = useState<[Dayjs, Dayjs]>([dayjs().startOf('year'), dayjs()]);
  const [rows, setRows] = useState<RestrictionSummaryRow[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    landuseApi
      .restrictionSummary(period[0].format('YYYY-MM-DD'), period[1].format('YYYY-MM-DD'))
      .then(setRows)
      .catch((e) => notify.error('Could not load the report', e.message))
      .finally(() => setLoading(false));
  }, [period, notify]);

  return (
    <AppCard
      title="Coverage report"
      extra={<AppButtonSecondary onClick={() => navigate('/landuse/rules')}>Back to rules</AppButtonSecondary>}
      style={{ margin: 24 }}
    >
      <Form layout="inline" style={{ marginBottom: 16 }}>
        <Form.Item label="Pre-checks saved">
          <DatePicker.RangePicker
            value={period}
            allowClear={false}
            onChange={(v) => v?.[0] && v[1] && setPeriod([v[0], v[1]])}
          />
        </Form.Item>
      </Form>
      <Table<RestrictionSummaryRow>
        rowKey="typeCode"
        loading={loading}
        dataSource={rows}
        pagination={false}
        size="small"
        columns={[
          { title: 'Code', dataIndex: 'typeCode' },
          { title: 'Restriction type', dataIndex: 'typeName' },
          { title: 'Rule in force today', dataIndex: 'ruleInForce', render: (v: boolean) => (v ? 'Yes' : 'No') },
          { title: 'Consent party', dataIndex: 'consentParty', render: (v?: string | null) => v || '—' },
          { title: 'Pre-checks in period', dataIndex: 'preCheckCount', align: 'right' },
          { title: 'Latest pre-check', dataIndex: 'latestPreCheck', render: (v?: string | null) => v || '—' },
        ]}
      />
      <Typography.Paragraph type="secondary" style={{ marginTop: 12 }}>
        Every restriction type is listed: a row with “No” and 0 means there is no rule in force for that type and
        no pre-check in the period showed it — that is the answer, not missing data.
      </Typography.Paragraph>
    </AppCard>
  );
};
