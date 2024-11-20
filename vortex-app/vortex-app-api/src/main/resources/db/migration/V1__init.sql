DO $$
BEGIN
  IF EXISTS(SELECT *
    FROM information_schema.columns
    WHERE table_name='vortex_order' and column_name='resource_type')
  THEN
    ALTER TABLE vortex_order DROP CONSTRAINT IF EXISTS vortex_order_resource_type_check;
    ALTER TABLE vortex_order ADD CONSTRAINT vortex_order_resource_type_check
     CHECK (((resource_type)::text = ANY ((ARRAY['PORT'::character varying, 'L2'::character varying, 'L3'::character varying])::text[])));
  END IF;

END $$;
