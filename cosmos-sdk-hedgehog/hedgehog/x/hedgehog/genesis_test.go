package hedgehog_test

import (
	"testing"

	"github.com/stretchr/testify/require"
	keepertest "hedgehog/testutil/keeper"
	"hedgehog/testutil/nullify"
	"hedgehog/x/hedgehog"
	"hedgehog/x/hedgehog/types"
)

func TestGenesis(t *testing.T) {
	genesisState := types.GenesisState{
		Params: types.DefaultParams(),

		// this line is used by starport scaffolding # genesis/test/state
	}

	k, ctx := keepertest.HedgehogKeeper(t)
	hedgehog.InitGenesis(ctx, *k, genesisState)
	got := hedgehog.ExportGenesis(ctx, *k)
	require.NotNil(t, got)

	nullify.Fill(&genesisState)
	nullify.Fill(got)

	// this line is used by starport scaffolding # genesis/test/assert
}
