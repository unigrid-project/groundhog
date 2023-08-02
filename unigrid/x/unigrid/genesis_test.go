package unigrid_test

import (
	"testing"

	"github.com/stretchr/testify/require"
	keepertest "unigrid/testutil/keeper"
	"unigrid/testutil/nullify"
	"unigrid/x/unigrid"
	"unigrid/x/unigrid/types"
)

func TestGenesis(t *testing.T) {
	genesisState := types.GenesisState{
		Params: types.DefaultParams(),

		// this line is used by starport scaffolding # genesis/test/state
	}

	k, ctx := keepertest.UnigridKeeper(t)
	unigrid.InitGenesis(ctx, *k, genesisState)
	got := unigrid.ExportGenesis(ctx, *k)
	require.NotNil(t, got)

	nullify.Fill(&genesisState)
	nullify.Fill(got)

	// this line is used by starport scaffolding # genesis/test/assert
}
